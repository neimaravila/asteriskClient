package br.com.virtualsistemas.asteriskclient.rs;

import java.util.List;

import javax.jws.WebMethod;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

public interface AsteriskClientService {

	@WebMethod
	Response registerReceiver(@FormParam("description") String description, 
			@FormParam("url") String url, 
			@FormParam("event") List<String> event);

	@WebMethod
	Response registerAsterisk(@FormParam("host") String host, 
			@FormParam("username") String username, 
			@FormParam("password") String password,
			@FormParam("port") Integer port);
}
