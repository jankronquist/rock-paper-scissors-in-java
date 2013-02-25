package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameWonEvent implements Event {
	public final UUID gameId;
	public final UUID winnerId;
	public final UUID loserId;

	public GameWonEvent(UUID gameId, UUID winnerId, UUID loserId) {
		this.gameId = gameId;
		this.winnerId = winnerId;
		this.loserId = loserId;
	}
}
