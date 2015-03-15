package com.jayway.rps.command;

import java.util.UUID;

import com.jayway.es.api.Command;

public class CreateGameCommand implements Command {
	public final UUID gameId;
	public final String playerEmail;
	
	public CreateGameCommand(UUID gameId, String playerEmail) {
		this.gameId = gameId;
		this.playerEmail = playerEmail;
	}

	@Override
	public UUID aggregateId() {
		return gameId;
	}
	
}
