package com.jayway.rps.event;

import com.jayway.es.api.Event;

import java.util.UUID;

public class GameCreatedEvent implements Event {
	public final UUID gameId;
	public final String playerEmail;
	
	GameCreatedEvent() {
	    gameId = null;
	    playerEmail = null;
    }
	
	public GameCreatedEvent(UUID gameId, String playerEmail) {
		this.gameId = gameId;
		this.playerEmail = playerEmail;
	}
}
