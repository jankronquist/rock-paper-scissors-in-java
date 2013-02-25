package com.jayway.es.store;

import com.jayway.es.api.Event;

public interface EventStream<V> extends Iterable<Event> {
	V version();
}
