package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameTiedEvent implements Event {
	public final UUID gameId;

	public GameTiedEvent(UUID gameId) {
		this.gameId = gameId;
	}
	
}
