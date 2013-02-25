package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;
import com.jayway.rps.Move;

public class MoveDecidedEvent implements Event {

	public final UUID gameId;
	public final String playerEmail;
	public final Move move;

	public MoveDecidedEvent(UUID gameId, String playerEmail, Move move) {
		this.gameId = gameId;
		this.playerEmail = playerEmail;
		this.move = move;
	}

}
