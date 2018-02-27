package br.com.virtualsistemas.asteriskclient.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import br.com.virtualsistemas.asteriskclient.model.Receiver;
import br.com.virtualsistemas.asteriskclient.utils.GenericDAO;
import br.com.virtualsistemas.asteriskclient.utils.QueryType;

@Startup
@ApplicationScoped
public class AsteriskEventMap {

	private Map<String, List<Receiver>> mapClients;
	
	@EJB
	private GenericDAO dao;
	
	
	private static final ScheduledExecutorService executorService = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), (run) -> {
				Thread thread = new Thread(run, "AsteriskEventMap");
				thread.setPriority(Thread.MIN_PRIORITY + 1);
				return thread;
			});
	
	@PostConstruct
	public void initialize(){
		mapClients = new HashMap<>();
		
		@SuppressWarnings("unchecked")
		List<Receiver> rec = (List<Receiver>) dao.findAll(Receiver.class, null);
		
		if(rec != null){
			List<Receiver> listReceivers = null;	
			for (Receiver receiver : rec) {
				if(receiver.getEvents().contains("all")){
					listReceivers = mapClients.get("all");
					if(listReceivers == null){
						listReceivers = new ArrayList<>();
						listReceivers.add(receiver);
						mapClients.put("all", listReceivers);
						break;
					} else{
						if(!listReceivers.contains(receiver)){
							listReceivers.add(receiver);
							mapClients.put("all", listReceivers);
							break;
						}
					}
					break;
				} else {
					List<Receiver> recEvt = null;
					for (String evt : receiver.getEvents()) {
						recEvt = mapClients.get(evt);
						if(recEvt == null){
							recEvt = new ArrayList<>();
							recEvt.add(receiver);
							mapClients.put(evt, recEvt);
						} else{
							if(!recEvt.contains(receiver)){
								recEvt.add(receiver);
								mapClients.put(evt, recEvt);
							}
						}
					}
				}
			}
		}
	}
	
	public Map<String, List<Receiver>> getMapClients() {
		return mapClients;
	}
	
	public Receiver register(List<String> events, String url, String description){
		Receiver r = getReceiverByUrl(url);
		if(r == null){
			r = new Receiver();
			r.setDescription(description);
			r.setIdentity(UUID.randomUUID().toString());
			r.setUrl(url);
			r.setEvents(events);
			dao.insert(r);
		} else{
			r.setEvents(events);
			r.setDescription(description);
			dao.update(r);
		}
		executorService.schedule(()->initialize(), 1, TimeUnit.SECONDS);
		return r;
		
	}
	
	private Receiver getReceiverByUrl(String url){
		Map<String, Object> params = new HashMap<>();
		params.put("url", url);
		return (Receiver) dao.getOneResult(QueryType.NAMED, Receiver.FIND_BY_URL, params);
	}
	
}
