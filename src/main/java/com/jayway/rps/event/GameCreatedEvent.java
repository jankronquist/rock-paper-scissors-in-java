package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameCreatedEvent implements Event {
	public final UUID gameId;
	public final String playerEmail;
	
	public GameCreatedEvent(UUID gameId, String playerEmail) {
		this.gameId = gameId;
		this.playerEmail = playerEmail;
	}
}
