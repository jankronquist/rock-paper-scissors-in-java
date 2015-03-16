package com.jayway.rps.infra.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class HandleAllExceptions implements ExceptionMapper<Exception> {

	@Override
	public Response toResponse(Exception exception) {
		return Response.status(400).entity(exception.getMessage()).build();
	}

}
