package com.jayway.es.store;

import java.util.List;
import java.util.UUID;

import com.jayway.es.api.Event;

public interface EventStore<V> {
	EventStream<Long> loadEventStream(UUID streamId);
	void store(UUID streamId, long version, List<? extends Event> events);
	EventStream<V> loadEventsAfter(V globalVersion);
}
