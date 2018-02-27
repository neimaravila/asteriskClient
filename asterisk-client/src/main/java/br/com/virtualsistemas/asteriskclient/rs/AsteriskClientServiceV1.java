package br.com.virtualsistemas.asteriskclient.rs;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.virtualsistemas.asteriskclient.facade.AsteriskEventMap;
import br.com.virtualsistemas.asteriskclient.model.AsteriskServer;
import br.com.virtualsistemas.asteriskclient.model.Receiver;
import br.com.virtualsistemas.asteriskclient.utils.GenericDAO;

@Path("/v1")
@RequestScoped
public class AsteriskClientServiceV1 implements AsteriskClientService {

	@Inject
	private Logger log;
	
	@Inject
	private GenericDAO dao;

	@Inject
	private AsteriskEventMap evtMap;


	@POST
	@Path("/registerReceiver")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response registerReceiver(@FormParam("description") String description, 
			@FormParam("url") String url, 
			@FormParam("event") List<String> event) {

		if(
				(description != null)&&
				(!description.isEmpty())&&
				(url != null)&&
				(!description.isEmpty())&&
				(event != null)&&
				(!event.isEmpty())
				){
			Receiver r = evtMap.register(event, url, description);
			if(r != null){
				log.info("Criando Receiver: " + r.getIdentity());
				return Response
						.status(Response.Status.OK)
						.entity(r).build();

			}

		} else{
			return Response.status(Response.Status.CONFLICT).build();
		}
		return Response.status(Response.Status.PARTIAL_CONTENT).build();

	}


	@POST
	@Path("/registerAsterisk")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response registerAsterisk(@FormParam("host") String host, 
			@FormParam("username") String username, 
			@FormParam("password") String password,
			@FormParam("port") Integer port) {
		
		
		if(
			(host != null)&&
			(!host.isEmpty())&&
			(username != null)&&
			(!username.isEmpty())&&
			(password != null)&&
			(!password.isEmpty())&&
			(port != null)&&
			(port.intValue() > 0)
				
				){
			AsteriskServer as = dao.find(AsteriskServer.class, host);
			if(as == null){
				as = new AsteriskServer();
				as.setHost(host);
				as.setPassword(password);
				as.setUsername(username);
				as.setPort(port);
				dao.insert(as);
				
			} else{
				as.setHost(host);
				as.setPassword(password);
				as.setUsername(username);
				as.setPort(port);
				dao.update(as);
			}
			
			log.info("Criando/Atualizando: " + as.getHost());
			
			return Response
					.status(Response.Status.OK)
					.entity(as).build();
		} else{
			return Response.status(Response.Status.PARTIAL_CONTENT).build();
		}
	}


}
