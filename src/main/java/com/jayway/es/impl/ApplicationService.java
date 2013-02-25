package com.jayway.es.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.jayway.es.api.Command;
import com.jayway.es.api.Event;
import com.jayway.es.store.EventStore;
import com.jayway.es.store.EventStream;

public class ApplicationService {
	private static final String HANDLE_METHOD = "handle";
	private final EventStore eventStore;
	private CommandHandlerLookup commandHandlerLookup;

	public ApplicationService(EventStore eventStore, Class<?>... aggregateTypes) {
		this.eventStore = eventStore;
		this.commandHandlerLookup = new CommandHandlerLookup(HANDLE_METHOD, aggregateTypes);
	}
	
	public void handle(Command command) throws Exception {
		EventStream<Long> eventStream = eventStore.loadEventStream(command.entityId());
		Object target = commandHandlerLookup.targetType(command).newInstance();
		for (Event event : eventStream) {
			handle(target, event);
		}
		List<Event> events = handle(target, command);
		if (events != null && events.size() > 0) {
			eventStore.store(command.entityId(), eventStream.version(), events);
		} else {
			// Command generated no events
		}
	}

	@SuppressWarnings("unchecked")
	private <R> R handle(Object target, Object param) throws Exception {
		Method method = target.getClass().getMethod(HANDLE_METHOD, param.getClass());
		try {
			return (R) method.invoke(target, param);
		} catch (InvocationTargetException e) {
			throw Sneak.sneakyThrow(e.getTargetException());
		}
	}
}
