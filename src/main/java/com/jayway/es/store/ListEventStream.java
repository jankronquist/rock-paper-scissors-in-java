package com.jayway.es.store;

import com.jayway.es.api.Event;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ListEventStream implements EventStream<Long> {
	private final long version;
	private final List<Event> events;
	
	public ListEventStream() {
		this.version = 0;
		events = Collections.emptyList();
	}

	public ListEventStream(long version, List<Event> events) {
		this.version = version;
		this.events = events;
	}
	
	public ListEventStream append(List<? extends Event> newEvents) {
		List<Event> events = new LinkedList<>(this.events);
		events.addAll(newEvents);
		return new ListEventStream(version+1, Collections.unmodifiableList(events));
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