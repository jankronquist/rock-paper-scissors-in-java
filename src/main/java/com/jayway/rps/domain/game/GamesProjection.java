package com.jayway.rps.domain.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jayway.rps.domain.Move;
import com.jayway.rps.domain.event.GameCreatedEvent;
import com.jayway.rps.domain.event.GameTiedEvent;
import com.jayway.rps.domain.event.GameWonEvent;
import com.jayway.rps.domain.event.MoveDecidedEvent;

public class GamesProjection {
	public static enum State {
		inProgress(false), 
		won(true), 
		tied(true);
		
		public final boolean completed;

		private State(boolean completed) {
			this.completed = completed;
		}
	}
	public static class GameState {
		public UUID gameId;
		public String createdBy;
		public Map<String, Move> moves = new HashMap<String, Move>();
		public State state;
		public String winner;
		public String loser;
	}
	private Map<UUID, GameState> games = new HashMap<>();
	
	public GameState get(UUID gameId) {
		return games.get(gameId);
	}

	public void handle(GameCreatedEvent e) {
		GameState game = new GameState();
		game.gameId = e.gameId;
		game.state = State.inProgress;
		games.put(e.gameId, game);
	}
	
	public void handle(MoveDecidedEvent e) {
		GameState game = games.get(e.gameId);
		game.moves.put(e.playerEmail, e.move);
	}

	public void handle(GameWonEvent e) {
		GameState game = games.get(e.gameId);
		game.state = State.won;
		game.winner = e.winnerEmail;
		game.loser = e.loserEmail;
	}

	public void handle(GameTiedEvent e) {
		GameState game = games.get(e.gameId);
		game.state = State.tied;
	}
}
