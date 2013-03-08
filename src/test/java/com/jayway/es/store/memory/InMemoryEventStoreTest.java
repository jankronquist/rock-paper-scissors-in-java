package com.jayway.es.store.memory;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import com.jayway.rps.event.GameTiedEvent;

public class InMemoryEventStoreTest {
	UUID gameId = UUID.randomUUID();

	@Test
	public void test() throws Exception {
		InMemoryEventStore es = new InMemoryEventStore();
		es.store(gameId, 0, Arrays.asList(new GameTiedEvent(gameId)));
		Thread.sleep(1);
		es.store(gameId, 1, Arrays.asList(new GameTiedEvent(gameId)));
	}
}
