package com.jayway.rps.game;

import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

import com.jayway.es.api.Event;
import com.jayway.es.impl.ApplicationService;
import com.jayway.es.store.EventStream;
import com.jayway.es.store.memory.InMemoryEventStore;
import com.jayway.rps.Move;
import com.jayway.rps.command.CreateGameCommand;
import com.jayway.rps.command.MakeMoveCommand;
import com.jayway.rps.event.GameTiedEvent;
import com.jayway.rps.event.GameWonEvent;

public class GameIntegrationTest {
	InMemoryEventStore eventStore = new InMemoryEventStore();
	ApplicationService application = new ApplicationService(eventStore, Game.class);
	UUID gameId = UUID.randomUUID();
	String player1 = UUID.randomUUID().toString();
	String player2 = UUID.randomUUID().toString();

	@Test
	public void tie() throws Exception {
		application.handle(new CreateGameCommand(gameId, player1, Move.rock));
		application.handle(new MakeMoveCommand(gameId, player2, Move.rock));
		assertEventStreamContains(gameId, new GameTiedEvent(gameId));
	}

	@Test
	public void victory() throws Exception {
		application.handle(new CreateGameCommand(gameId, player1, Move.rock));
		application.handle(new MakeMoveCommand(gameId, player2, Move.paper));
		assertEventStreamContains(gameId, new GameWonEvent(gameId, player2, player1));
	}

	@Test(expected=IllegalArgumentException.class)
	public void same_player_should_fail() throws Exception {
		application.handle(new CreateGameCommand(gameId, player1, Move.rock));
		application.handle(new MakeMoveCommand(gameId, player1, Move.rock));
	}

	@Test(expected=IllegalStateException.class)
	public void game_not_started() throws Exception {
		application.handle(new MakeMoveCommand(gameId, player1, Move.rock));
	}

	@Test(expected=IllegalStateException.class)
	public void move_after_end_should_fail() throws Exception {
		application.handle(new CreateGameCommand(gameId, player1, Move.rock));
		application.handle(new MakeMoveCommand(gameId, player2, Move.rock));
		application.handle(new MakeMoveCommand(gameId, player2, Move.rock));
	}

	private void assertEventStreamContains(UUID streamId, Event expectedEvent) {
		EventStream eventStream = eventStore.loadEventStream(gameId);
		String expected = EventStringUtil.toString(expectedEvent);
		for (Event event : eventStream) {
			if (EventStringUtil.toString(event).equals(expected)) return;
		}
		fail("Expected event did not occur: " + expected);
	}
}
