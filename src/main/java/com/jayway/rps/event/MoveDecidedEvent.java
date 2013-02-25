package com.jayway.rps.event;

import java.util.UUID;

import com.jayway.es.api.Event;
import com.jayway.rps.Move;

public class MoveDecidedEvent implements Event {

	public final UUID gameId;
	public final UUID playerId;
	public final Move move;

	public MoveDecidedEvent(UUID gameId, UUID playerId, Move move) {
		this.gameId = gameId;
		this.playerId = playerId;
		this.move = move;
	}

}
