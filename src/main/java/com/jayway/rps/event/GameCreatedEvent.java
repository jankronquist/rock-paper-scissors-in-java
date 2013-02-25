package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameCreatedEvent implements Event {
	public final UUID gameId;
	public final UUID playerId;
	
	public GameCreatedEvent(UUID gameId, UUID playerId) {
		this.gameId = gameId;
		this.playerId = playerId;
	}
}
