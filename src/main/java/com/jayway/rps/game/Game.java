package com.jayway.rps.game;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.jayway.es.api.Event;
import com.jayway.rps.Move;
import com.jayway.rps.command.CreateGameCommand;
import com.jayway.rps.command.MakeMoveCommand;
import com.jayway.rps.event.GameCreatedEvent;
import com.jayway.rps.event.GameTiedEvent;
import com.jayway.rps.event.GameWonEvent;
import com.jayway.rps.event.MoveDecidedEvent;

public class Game {
	enum State {
		notStarted, created, tied, won
	}
	private State state = State.notStarted;
	private UUID creatorId;
	private Move move;

	public List<? extends Event> handle(CreateGameCommand c) {
		if (state != State.notStarted) throw new IllegalStateException(state.toString());
		return Arrays.asList(
				new GameCreatedEvent(c.gameId, c.playerId), 
				new MoveDecidedEvent(c.gameId, c.playerId, c.move));
	}

	public List<? extends Event> handle(MakeMoveCommand c) {
		if (State.created != state) throw new IllegalStateException(state.toString());
		if (creatorId.equals(c.playerId)) throw new IllegalArgumentException("Player already in game");
		
		return Arrays.asList(
				new MoveDecidedEvent(c.gameId, c.playerId, c.move),
				makeEndGameEvent(c.gameId, c.playerId, c.move));
	}

	private Event makeEndGameEvent(UUID gameId, UUID opponentId, Move opponentMove) {
		if (move.defeats(opponentMove)) {
			return new GameWonEvent(gameId, creatorId, opponentId);
		} else if (opponentMove.defeats(move)) {
			return new GameWonEvent(gameId, opponentId, creatorId);
		} else {
			return new GameTiedEvent(gameId);
		}
	}

	public void handle(GameCreatedEvent e) {
		state = State.created;
		creatorId = e.playerId;
	}
	
	public void handle(MoveDecidedEvent e) {
		move = e.move;
	}

	public void handle(GameWonEvent e) {
		state = State.won;
	}

	public void handle(GameTiedEvent e) {
		state = State.tied;
	}
}
