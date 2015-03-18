package com.jayway.es.store.eventstore;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.es.api.Event;
import com.jayway.es.impl.Sneak;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;
import com.jayway.es.store.ListEventStream;
import com.jayway.rps.app.RpsConfig;

import eventstore.EsException;
import eventstore.EventData;
import eventstore.EventNumber.Exact;
import eventstore.IndexedEvent;
import eventstore.Position;
import eventstore.ReadStreamEventsCompleted;
import eventstore.StreamNotFoundException;
import eventstore.SubscriptionObserver;
import eventstore.WriteEventsCompleted;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.EventDataBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;

public class EventStoreEventStore implements EventStore<Long> {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreEventStore.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ActorSystem system;
    private final String streamPrefix;
    private final ActorRef connectionActor;
    private final ActorRef writeResult;
    private final ObjectMapper mapper;
    private final EsConnection connection;

    public EventStoreEventStore(String streamPrefix, ObjectMapper mapper) {
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;
        this.system = ActorSystem.create();
        connectionActor = system.actorOf(ConnectionActor.getProps());
        writeResult = system.actorOf(Props.create(WriteResult.class));
        this.connection = EsConnectionFactory.create(system);
    }
    
	@Override
	public EventStream<Long> loadEventStream(UUID aggregateId) {
	    final Future<ReadStreamEventsCompleted> future = connection.readStreamEventsForward(streamPrefix + aggregateId, null, 1000, false, null);
	    try {
            ReadStreamEventsCompleted result = future.result(Duration.apply(10, TimeUnit.SECONDS), null);
            List<Event> events = new ArrayList<>();
            for (eventstore.Event event : result.eventsJava()) {
                Event domainEvent = parse(event);
				events.add(domainEvent);
            }
            return new ListEventStream(result.lastEventNumber().value(), events);
	    } catch (StreamNotFoundException e) {
            return new ListEventStream(-1, Collections.emptyList());
        } catch (Exception e) {
            throw Sneak.sneakyThrow(e);
        }
	}

	private Event parse(eventstore.Event event)
			throws ClassNotFoundException, IOException, JsonParseException,
			JsonMappingException {
		Class<? extends Event> type = (Class<? extends Event>) Class.forName(event.data().eventType());
		String json = new String(event.data().data().value().toArray(), UTF8);
		Event domainEvent = mapper.readValue(json, type);
		return domainEvent;
	}

	@Override
	public void store(UUID aggregateId, long version, List<Event> events) {
        WriteEventsBuilder builder = new WriteEventsBuilder(streamPrefix + aggregateId);
	    for (Event event : events) {
	        builder = builder.addEvent(toEventData(event));
        }
	    if (version >= 0) {
	        builder = builder.expectVersion((int) version);
	    } else {
	        builder = builder.expectNoStream();
	    }
        connectionActor.tell(builder.build(), writeResult);
	}

    private EventData toEventData(Event event) {
        try {
            return new EventDataBuilder(event.getClass().getName())
            .eventId(UUID.randomUUID())
            .jsonData(mapper.writeValueAsString(event))
            .build();
        } catch (JsonProcessingException e) {
            throw Sneak.sneakyThrow(e);
        }
    }
	
	public static class WriteResult extends UntypedActor {
        final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof WriteEventsCompleted) {
                final WriteEventsCompleted completed = (WriteEventsCompleted) message;
                log.info("range: {}, position: {}", completed.numbersRange(), completed.position());
            } else if (message instanceof Status.Failure) {
                final Status.Failure failure = ((Status.Failure) message);
                final EsException exception = (EsException) failure.cause();
                log.error(exception, exception.toString());
            } else {
                unhandled(message);
            }
        }
    }

	@Override
	public Observable<Event> all() {
		return Observable.create(subscriber -> {
	        connection.subscribeToAllFrom(new SubscriptionObserver<IndexedEvent>() {
				@Override
				public void onLiveProcessingStart(Closeable arg0) {
				}
				
				@Override
				public void onEvent(IndexedEvent event, Closeable arg1) {
					if (!event.event().streamId().isSystem() && event.event().streamId().streamId().startsWith("game")) {
						try {
							subscriber.onNext(parse(event.event()));
						} catch (Exception e) {
							logger.warn("Error when handling event", e);
						}
					}
				}
				
				@Override
				public void onError(Throwable e) {
					subscriber.onError(e);
				}
				
				@Override
				public void onClose() {
					subscriber.onCompleted();
				}
			}, null, true, null);
		});
	}
	
}
