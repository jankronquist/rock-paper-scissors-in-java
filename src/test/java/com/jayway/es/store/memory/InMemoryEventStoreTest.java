package com.jayway.es.store.memory;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import com.jayway.es.api.Event;
import com.jayway.es.store.EventStream;
import com.jayway.rps.event.GameTiedEvent;

public class InMemoryEventStoreTest {
	UUID gameId = UUID.randomUUID();

	@Test
	public void test() throws Exception {
		InMemoryEventStore es = new InMemoryEventStore();
		es.store(gameId, 0, Arrays.asList(new GameTiedEvent(gameId)));
		Thread.sleep(1);
		es.store(gameId, 1, Arrays.asList(new GameTiedEvent(gameId)));
		EventStream<Long> stream = es.loadEventsAfter(0L);
		assertEquals(1, countEvents(stream));
		Long id = stream.version();
		System.out.println("id=" + id);
	}

	private int countEvents(EventStream<Long> stream) {
		int result = 0;
		for (Event event : stream) {
			result++;
		}
		return result;
	}

}
