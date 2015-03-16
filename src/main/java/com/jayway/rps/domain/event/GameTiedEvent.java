package com.jayway.rps.domain.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameTiedEvent implements Event {
	public final UUID gameId;
	
	GameTiedEvent() {
		gameId = null;
	}

	public GameTiedEvent(UUID gameId) {
		this.gameId = gameId;
	}
	
}
