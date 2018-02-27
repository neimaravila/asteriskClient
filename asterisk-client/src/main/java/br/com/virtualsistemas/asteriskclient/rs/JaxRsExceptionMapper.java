package br.com.virtualsistemas.asteriskclient.rs;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider  
public class JaxRsExceptionMapper implements ExceptionMapper<WebApplicationException> {  

	@Override  
	public Response toResponse(WebApplicationException exception) {  
		return Response.status(exception.getResponse().getStatus()).build();  
	}
}