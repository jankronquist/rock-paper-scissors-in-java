package com.jayway.rps.infra.rest;

import java.util.UUID;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.jayway.es.impl.ApplicationService;
import com.jayway.rps.domain.Move;
import com.jayway.rps.domain.command.CreateGameCommand;
import com.jayway.rps.domain.command.MakeMoveCommand;

@Path("games")
public class RpsResource {
    private ApplicationService applicationService;

	public RpsResource(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@POST
    public Response createGame(@HeaderParam("SimpleIdentity") String email) throws Exception {
		UUID gameId = UUID.randomUUID();
		applicationService.handle(new CreateGameCommand(gameId, email));
		return Response.created(UriBuilder.fromMethod(RpsResource.class, "game").build(gameId.toString())).build();
    }

	@GET
	@Path("{gameId}")
    public GameDTO game(
    		@PathParam("gameId") String gameId, 
    		@HeaderParam("SimpleIdentity") String email) throws Exception {
		return null;
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
