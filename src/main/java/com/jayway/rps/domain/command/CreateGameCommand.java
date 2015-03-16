package com.jayway.rps.domain.command;

import java.util.UUID;

import com.jayway.es.api.Command;

public class CreateGameCommand implements Command {
	public final UUID gameId;
	public final String playerEmail;
	
	public CreateGameCommand(UUID gameId, String playerEmail) {
		if (gameId == null) throw new IllegalArgumentException("gameId must not be null");
		if (playerEmail == null) throw new IllegalArgumentException("playerEmail must not be null");
		this.gameId = gameId;
		this.playerEmail = playerEmail;
	}

	@Override
	public UUID aggregateId() {
		return gameId;
	}
	
}
