package com.jayway.es.impl;

import java.util.List;

import com.jayway.es.api.Command;
import com.jayway.es.api.Event;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;

public class ApplicationService {
	private final EventStore eventStore;
	private CommandHandlerLookup commandHandlerLookup;

	public ApplicationService(EventStore eventStore, Class<?>... aggregateTypes) {
		this.eventStore = eventStore;
		this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
	}
	
	public void handle(Command command) throws Exception {
		EventStream<Long> eventStream = eventStore.loadEventStream(command.aggregateId());
		Object target = newAggregateInstance(command);
		for (Event event : eventStream) {
			ReflectionUtil.invokeHandleMethod(target, event);
		}
		List<Event> events = ReflectionUtil.invokeHandleMethod(target, command);
		if (events != null && events.size() > 0) {
			eventStore.store(command.aggregateId(), eventStream.version(), events);
		} else {
			// Command generated no events
		}
	}

	private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
		return commandHandlerLookup.targetType(command).newInstance();
	}
}
