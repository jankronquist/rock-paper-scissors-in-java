package com.jayway.rps.command;

import java.util.UUID;

import com.jayway.es.api.Command;
import com.jayway.rps.Move;

public class CreateGameCommand implements Command {
	public final UUID gameId;
	public final String playerEmail;
	public final Move move;
	
	public CreateGameCommand(UUID gameId, String playerEmail, Move move) {
		this.gameId = gameId;
		this.playerEmail = playerEmail;
		this.move = move;
	}

	@Override
	public UUID aggregateId() {
		return gameId;
	}
	
}
