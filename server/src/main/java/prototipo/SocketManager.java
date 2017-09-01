package prototipo;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.type.builtin.StringDataReader;

public class SocketManager extends Thread {

	HelloPublisher pub;
	Thread subThread;
	private Principal principal;
	static SocketIOServer server;

	public SocketManager() {
	}

	public SocketManager(Principal principal) {
		this.principal = principal;
	}

	static SocketIOServer servidor;

	public void run() {

		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(9092);

		final SocketIOServer server = new SocketIOServer(config);
		SocketManager.server = server;

		System.out.println("criando socket manager... " + SocketManager.server);

		server.addEventListener("mensagem_do_browser", ChatObject.class, new DataListener<ChatObject>() {
			
			@Override
			public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) {

				pub.escrever(data.getFrom(), data.getTo(), data.getMessage());

			}
			
		});
		
//		server.addEventListener("cliente_saindo", ChatObject.class, new DataListener<ChatObject>() {
//			
//			@Override
//			public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) {
//
//				pub.dispose();
//
//			}
//			
//		});
		
	

		server.start();

		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		server.stop();


	}

	public SocketIOServer getServer() {
		return server;
	}

	public void setServer(SocketIOServer server) {
		SocketManager.server = server;
	}

	public HelloPublisher getPub() {
		return pub;
	}

	public void setPub(HelloPublisher pub) {
		this.pub = pub;
	}


}
