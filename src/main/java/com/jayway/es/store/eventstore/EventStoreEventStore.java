package com.jayway.es.store.eventstore;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.es.api.Event;
import com.jayway.es.impl.Sneak;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;
import com.jayway.es.store.ListEventStream;
import com.jayway.rps.event.GameCreatedEvent;
import eventstore.EsException;
import eventstore.EventData;
import eventstore.ReadStreamEventsCompleted;
import eventstore.WriteEventsCompleted;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.EventDataBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EventStoreEventStore implements EventStore<Long> {
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        EventStoreEventStore store = new EventStoreEventStore("aggregate-", new ObjectMapper());
        UUID id = UUID.randomUUID();
        store.store(id, 0, Arrays.asList(new GameCreatedEvent(id, "test@somewhere")));
        Thread.sleep(5000);
        EventStream<Long> stream = store.loadEventStream(id);
        for (Event event : stream) {
            System.out.println(((GameCreatedEvent)event).playerEmail);
        }
        Thread.sleep(5000);
    }
    
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
                Class<? extends Event> type = (Class<? extends Event>) Class.forName(event.data().eventType());
                String json = new String(event.data().data().value().toArray(), UTF8);
                events.add(mapper.readValue(json, type));
            }
            return new ListEventStream(result.lastEventNumber().value(), events);
        } catch (Exception e) {
            throw Sneak.sneakyThrow(e);
        }
	}

	@Override
	public void store(UUID aggregateId, long version, List<? extends Event> events) {
        WriteEventsBuilder builder = new WriteEventsBuilder(streamPrefix + aggregateId);
	    for (Event event : events) {
	        builder = builder.addEvent(toEventData(event));
        }
	    if (version > 0) {
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

	
}
