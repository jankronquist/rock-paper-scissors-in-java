package com.jayway.rps.domain.command;

import java.util.UUID;

import com.jayway.es.api.Command;
import com.jayway.rps.domain.Move;

public class MakeMoveCommand implements Command {
	public final UUID gameId;
	public final String playerEmail;
	public final Move move;

	public MakeMoveCommand(UUID gameId, String playerEmail, Move move) {
		if (gameId == null) throw new IllegalArgumentException("gameId must not be null");
		if (playerEmail == null) throw new IllegalArgumentException("playerEmail must not be null");
		if (move == null) throw new IllegalArgumentException("move must not be null");
		this.gameId = gameId;
		this.playerEmail = playerEmail;
		this.move = move;
		
	}

	@Override
	public UUID aggregateId() {
		return gameId;
	}
}
