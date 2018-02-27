package br.com.virtualsistemas.asteriskclient.facade;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gson.Gson;

import br.com.virtualsistemas.asteriskclient.model.AsteriskEvent;
import br.com.virtualsistemas.asteriskclient.model.Receiver;


@Startup
@Singleton
public class AsteriskManagerMonitoringService implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final ScheduledExecutorService executorService = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), (run) -> {
				Thread thread = new Thread(run, "AsteriskManagerMonitoringService");
				thread.setPriority(Thread.MIN_PRIORITY + 1);
				return thread;
			});

	@Inject
	private Logger log;

	@Inject
	private AsteriskEventMap evtMap;

	@Asynchronous
	public void receiveEvent(@Observes AsteriskEvent event){

		Gson gson = new Gson();
		String evtJson = gson.toJson(event);
		
		List<Receiver> lrAll = evtMap.getMapClients().get("all");

		if(lrAll != null){
			for (Receiver r : lrAll) {
				sendEvent(r,evtJson);
			}
		}

		List<Receiver> lr = evtMap.getMapClients().get(event.getName());
		if(lr != null){
			for (Receiver r : lr) {
				executorService.execute(()->sendEvent(r,evtJson));
			}
		}

	}

	private void sendEvent(Receiver r, String evtJson) {
		try {

			URL url = new URL(r.getUrl());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

			out.write(evtJson);
			out.close();
			InputStream is = null;
			if(connection.getResponseCode() != 200){
				is = connection.getErrorStream();
			} else{
				is = connection.getInputStream();
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(is));
			String retorno = in.readLine();
			in.close();
			log.info("RETORNO DE: " + r.getIdentity() + " | " + retorno);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
