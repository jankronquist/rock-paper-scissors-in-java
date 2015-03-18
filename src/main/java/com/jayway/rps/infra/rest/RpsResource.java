package com.jayway.rps.infra.rest;

import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.jayway.es.impl.ApplicationService;
import com.jayway.rps.domain.Move;
import com.jayway.rps.domain.command.CreateGameCommand;
import com.jayway.rps.domain.command.MakeMoveCommand;
import com.jayway.rps.domain.game.GamesProjection;
import com.jayway.rps.domain.game.GamesProjection.GameState;

@Path("games")
public class RpsResource {
    private ApplicationService applicationService;
	private GamesProjection gamesProjection;

	public RpsResource(ApplicationService applicationService, GamesProjection gamesProjection) {
		this.applicationService = applicationService;
		this.gamesProjection = gamesProjection;
	}

	@POST
    public Response createGame(@HeaderParam("SimpleIdentity") String email) throws Exception {
		UUID gameId = UUID.randomUUID();
		applicationService.handle(new CreateGameCommand(gameId, email));
		return Response.created(UriBuilder.fromMethod(RpsResource.class, "game").build(gameId.toString())).build();
    }

	@GET
	@Path("{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
    public GameDTO game(
    		@PathParam("gameId") String gameId) throws Exception {
		GameState gameState = gamesProjection.get(UUID.fromString(gameId));
		GameDTO dto = new GameDTO();
		dto.gameId = gameState.gameId.toString();
		dto.createdBy = gameState.createdBy;
		dto.state = gameState.state.toString();
		if (gameState.state.completed) {
			dto.winner = gameState.winner;
			dto.loser = gameState.loser;
			dto.moves = gameState.moves;
		}
		return dto;
    }

	@POST
	@Path("{gameId}")
    public void makeMove(
    		@PathParam("gameId") String gameId, 
    		@HeaderParam("SimpleIdentity") String email,
    		@FormParam("move") Move move) throws Exception {
		
		applicationService.handle(new MakeMoveCommand(UUID.fromString(gameId), email, move));
    }
}
