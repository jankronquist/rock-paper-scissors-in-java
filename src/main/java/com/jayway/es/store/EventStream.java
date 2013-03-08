package com.jayway.es.store;

import com.jayway.es.api.Event;

public interface EventStream extends Iterable<Event> {
	long version();
}
