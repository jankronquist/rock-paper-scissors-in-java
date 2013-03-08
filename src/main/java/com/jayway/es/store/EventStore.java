package com.jayway.es.store;

import java.util.List;
import java.util.UUID;

import com.jayway.es.api.Event;

public interface EventStore {
	EventStream loadEventStream(UUID aggregateId);
	void store(UUID aggregateId, long version, List<? extends Event> events);
}
