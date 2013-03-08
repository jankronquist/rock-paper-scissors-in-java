package com.jayway.es.store.memory;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.jayway.es.api.Event;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;

public class InMemoryEventStore implements EventStore {
	private final Map<UUID, InMemoryEventStream> streams = new ConcurrentHashMap<UUID, InMemoryEventStream>();

	@Override
	public InMemoryEventStream loadEventStream(UUID aggregateId) {
		InMemoryEventStream eventStream = streams.get(aggregateId);
		if (eventStream == null) {
			eventStream = new InMemoryEventStream();
			streams.put(aggregateId, eventStream);
		}
		return eventStream;
	}

	@Override
	public void store(UUID aggregateId, long version, List<? extends Event> events) {
		InMemoryEventStream stream = loadEventStream(aggregateId);
		if (stream.version() != version) {
			throw new ConcurrentModificationException("Stream has already been modified");
		}
		streams.put(aggregateId, stream.append(events));
	}	
}

class InMemoryEventStream implements EventStream {
	private final long version;
	private final List<Event> events;
	
	public InMemoryEventStream() {
		this.version = 0;
		events = Collections.emptyList();
	}

	public InMemoryEventStream(long version, List<Event> events) {
		this.version = version;
		this.events = events;
	}
	
	public InMemoryEventStream append(List<? extends Event> newEvents) {
		List<Event> events = new LinkedList<Event>(this.events);
		events.addAll(newEvents);
		return new InMemoryEventStream(version+1, Collections.unmodifiableList(events));
	}

	@Override
	public Iterator<Event> iterator() {
		return events.iterator();
	}

	@Override
	public long version() {
		return version;
	}
	
}
