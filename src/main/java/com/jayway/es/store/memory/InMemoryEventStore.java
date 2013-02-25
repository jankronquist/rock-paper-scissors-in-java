package com.jayway.es.store.memory;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.jayway.es.api.Event;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;

public class InMemoryEventStore implements EventStore<Long> {
	private final Map<UUID, InMemoryEventStream> streams = new ConcurrentHashMap<UUID, InMemoryEventStream>();
	private final TreeSet<Transaction> transactions = new TreeSet<Transaction>();

	@Override
	public InMemoryEventStream loadEventStream(UUID streamId) {
		InMemoryEventStream eventStream = streams.get(streamId);
		if (eventStream == null) {
			eventStream = new InMemoryEventStream();
			streams.put(streamId, eventStream);
		}
		return eventStream;
	}

	@Override
	public void store(UUID streamId, long version, List<? extends Event> events) {
		InMemoryEventStream stream = loadEventStream(streamId);
		if (stream.version() != version) {
			throw new ConcurrentModificationException("Stream has already been modified");
		}
		streams.put(streamId, stream.append(events));
		synchronized (transactions) {
			transactions.add(new Transaction(events));
		}
	}
	
	@Override
	public EventStream<Long> loadEventsAfter(Long timestamp) {
		// include all events after this timestamp, except the events with the current timestamp
		// since new events might be added with the current timestamp
		List<Event> events = new LinkedList<Event>();
		long now;
		synchronized (transactions) {
			now = System.currentTimeMillis();
			for (Transaction t : transactions.tailSet(new Transaction(timestamp)).headSet(new Transaction(now))) {
				events.addAll(t.events);
			}
		}
		return new InMemoryEventStream(now-1, events);
	}
	
}

class Transaction implements Comparable<Transaction> {
	public final List<? extends Event> events;
	private final long timestamp;
	
	public Transaction(long timestamp) {
		events = Collections.emptyList();
		this.timestamp = timestamp;
		
	}
	public Transaction(List<? extends Event> events) {
		this.events = events;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public int compareTo(Transaction other) {
		if (timestamp < other.timestamp) {
			return -1;
		} else if (timestamp > other.timestamp) {
			return 1;
		}
		return 0;
	}
}

class InMemoryEventStream implements EventStream<Long> {
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
	public Long version() {
		return version;
	}
	
}
