package br.com.virtualsistemas.asteriskclient.facade;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.ManagerEventListenerProxy;
import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.response.CommandResponse;

import br.com.virtualsistemas.asteriskclient.model.AsteriskEvent;
import br.com.virtualsistemas.asteriskclient.model.AsteriskServer;
import br.com.virtualsistemas.asteriskclient.utils.GenericDAO;

@Startup
@Singleton
public class AsteriskServerConn implements ManagerEventListener{

	private ManagerConnectionFactory FACTORY;
	

	private static final ScheduledExecutorService schedule = Executors
			.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), (run) -> {
				Thread thread = new Thread(run, "AsteriskManagerMonitoringServiceImplThread");
				thread.setPriority(Thread.MIN_PRIORITY + 1);
				return thread;
			});

	private transient static ManagerConnection srvManagerEvent = null;
	private transient static ExecutorService executor = Executors.newSingleThreadExecutor();

	@Inject
	private Logger log;
	
	@EJB
	private GenericDAO dao;

	@Any
	@Inject
	private Event<AsteriskEvent> evtManagerEvent;

	private void createServidorManager(AsteriskServer as) {
		if (srvManagerEvent == null) {
			FACTORY = new ManagerConnectionFactory(as.getHost(),as.getPort(), as.getUsername(), as.getPassword());
			srvManagerEvent = FACTORY.createManagerConnection();
			try {
				log.info("Logging on Asterisk Server - Manager Connection");
				srvManagerEvent.login();
				srvManagerEvent.addEventListener(new ManagerEventListenerProxy(this));
			} catch (Exception e) {
				log.warning("Falha na Criação do Servidor Manager");
			}
		}
	}

	
	@PostConstruct
	public void poll() {
		Runnable tarefa = new Runnable() {
			public void run() {
				
				if(initialize()){
					log.info("Iniciando Conexao com o Asterisk");
				} else{
					
					log.info("Aguardando configuração com o Asterisk");
					schedule.schedule(this, 1, TimeUnit.MINUTES);
				}
			}
		};
		tarefa.run();
	}
	
	
	public boolean initialize() {
		
		@SuppressWarnings("unchecked")
		List<AsteriskServer> las = (List<AsteriskServer>) dao.findAll(AsteriskServer.class, null);
		
		if((las != null)&&(!las.isEmpty())){
			createServidorManager(las.get(0));
			return true;
		} else{
			return false;
		}
		
	}

	@PreDestroy
	protected void shutdown() {
		log.info("Tentando Deslogar");
		AsteriskServerConn.srvManagerEvent.logoff();
		executor.shutdown();
	}

	public ManagerConnection getSrvManagerEvent() {
		return srvManagerEvent;
	}

	public void sendAction(ManagerAction action, SendActionCallback callback) {
		try {
			ManagerConnection con = srvManagerEvent;
			con.sendAction(action, callback);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final long sendCommandActionTimeout = 1000 * 60;

	public void sendCommandAction(String command, List<String> retorno) {
		try {
			sendAction(new CommandAction(command), response -> {
				try {
					List<String> result = ((CommandResponse) response).getResult();
					if (!((result == null) || (result.isEmpty()))) {
						retorno.addAll(result);
					}
				} finally {
					synchronized (retorno) {
						retorno.notifyAll();
					}
				}
			});
			synchronized (retorno) {
				retorno.wait(sendCommandActionTimeout);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void onManagerEvent(ManagerEvent event) {
		AsteriskEvent ae = new AsteriskEvent();
		ae.setDateReceived(new Date());
		ae.setName(event.getClass().getSimpleName());
		ae.setEvent(event);
		executor.execute(() -> evtManagerEvent.fire(ae));
		
	}

}