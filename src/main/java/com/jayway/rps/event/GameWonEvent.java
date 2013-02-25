package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;

public class GameWonEvent implements Event {
	public final UUID gameId;
	public final String winnerEmail;
	public final String loserEmail;

	public GameWonEvent(UUID gameId, String winnerEmail, String loserEmail) {
		this.gameId = gameId;
		this.winnerEmail = winnerEmail;
		this.loserEmail = loserEmail;
	}
}
