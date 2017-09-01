package prototipo;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.infrastructure.DomainParticipantConfigParams_t;
import com.rti.dds.infrastructure.InstanceHandleSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.SampleInfo;

public class Principal {

	Thread serverThread;
	Thread pubThread;
	Thread subThread;
	HelloPublisher pub;
	HelloSubscriber sub;
	private SocketManager socketManager;
	private Thread monitorThread;
	private DomainParticipant participant;
	private int domainId;
	private int contador;


	protected void dispose() {                

		if (pub.dynamicWriter != null && pub.instance != null) {                      
			pub.dynamicWriter.delete_data(pub.instance);
		}    

		if (this.participant != null) {
			this.participant.delete_contained_entities();
			pub.dynamicWriter = null;
			sub.dynamicReader = null;

			DomainParticipantFactory.get_instance().delete_participant(
					this.participant);                
		}

		DomainParticipantFactory.finalize_instance();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void iniciar(String nome) {

				
		if (this.participant == null) {


			DomainParticipantConfigParams_t conf = new DomainParticipantConfigParams_t();
			conf.participant_name = nome;
			
			this.participant = DomainParticipantFactory.get_instance()
					.create_participant_from_config_w_params(
							"MyParticipantLibrary::Participante1",
							conf)
					;

			
			if (this.participant == null) {
				System.out.println("! Unable to create DDS domain participant");
				return;
			}

		}


		pub = new HelloPublisher(participant);
		socketManager.setPub(pub);
		pub.start();

	
		sub = new HelloSubscriber(socketManager, participant);
		subThread = new Thread(sub);
		subThread.start();
		

	}

	public void preparar() {
		domainId = 0;

		socketManager = new SocketManager();
		socketManager.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		monitorThread = new Thread(new MonitorDiscoveryInformation(socketManager));
		monitorThread.start();	

		
		socketManager.server.addEventListener("login", ChatObject.class, new DataListener<ChatObject>() {
			
			@Override
			public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) {
				
				iniciar(data.getMessage());

			}
			
		});
		
		
		socketManager.server.addConnectListener( new ConnectListener() {

			@Override
			public void onConnect(SocketIOClient arg0) {

				System.out.println("## cliente conectou");
//				iniciar();
			}
		});

		socketManager.server.addDisconnectListener(new DisconnectListener() {

			@Override
			public void onDisconnect(SocketIOClient arg0) {

//				pub.dispose();
				System.out.println("## cliente desconectou" );


			}
		});


	}


	public static void main(String[] args) {
		
//		if (args.length > 0) {
//		 
//			
//		}

		String NDDSHOME = System.getenv("NDDSHOME");  
		String DYLD_LIBRARY_PATH = System.getenv("DYLD_LIBRARY_PATH");
		String LD_LIBRARY_PATH = System.getenv("LD_LIBRARY_PATH");
		System.out.println("NDDSHOME="+ NDDSHOME);  
		System.out.println("DYLD_LIBRARY_PATH="+ DYLD_LIBRARY_PATH); 
		System.out.println("LD_LIBRARY_PATH="+ LD_LIBRARY_PATH);

		new Principal().preparar();

	}



	public Thread getServerThread() {
		return serverThread;
	}



	public void setServerThread(Thread serverThread) {
		this.serverThread = serverThread;
	}



	public Thread getPubThread() {
		return pubThread;
	}



	public void setPubThread(Thread pubThread) {
		this.pubThread = pubThread;
	}



	public Thread getSubThread() {
		return subThread;
	}



	public void setSubThread(Thread subThread) {
		this.subThread = subThread;
	}



	public HelloPublisher getPub() {
		return pub;
	}



	public void setPub(HelloPublisher pub) {
		this.pub = pub;
	}

}
