/* (c) Copyright, Real-Time Innovations, 2012.  All rights reserved. 
 * Author:  Gerardo Pardo-Castellote 
 */

package prototipo;

import java.util.ArrayList;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.rti.dds.domain.*;
import com.rti.dds.domain.builtin.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.builtin.*;
import com.rti.dds.subscription.*;
import com.rti.dds.subscription.builtin.*;
import com.rti.ndds.config.Version;


public class MonitorDiscoveryInformation implements Runnable {
	static int verbosity  = 1;

	int domainId;
	DomainParticipant participant;
	ParticipantBuiltinTopicDataDataReader   participantsDR;
	PublicationBuiltinTopicDataDataReader   publicationsDR;
	SubscriptionBuiltinTopicDataDataReader  subscriptionsDR;

	SocketManager socketManager;


	public MonitorDiscoveryInformation(SocketManager socketManager) {
		super();
		this.socketManager = socketManager;
	}


	public MonitorDiscoveryInformation() {
		super();
	}




	final static int MAX_ACTIVE_CONDITIONS = 3; // We will only install e conditions on the
	ConditionSeq activeConditionSeq;
	private WaitSet discoveryWaitSet;

	public boolean start(int theDomainId) {        
		Version version = Version.get_instance ();
		System.out.println("Running RTI Connext version: " + version);

		domainId = theDomainId;
		DomainParticipantFactory factory = DomainParticipantFactory.get_instance();        
		DomainParticipantQos pQos = new DomainParticipantQos();

		factory.get_default_participant_qos(pQos);
		pQos.participant_name.name = "RTI Connext Spy Discovery Snippet";
		try {
			participant = factory.create_participant(
					domainId, 
					pQos, //DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
					null, // listener
					StatusKind.STATUS_MASK_NONE);
		} catch ( Exception e) {
			String lastStartError = "Error creating the DDS domain. Common causes are:"
					+ "\n   - Lack of a network. E.g disconected wireless."
					+ "\n   - A network interface that does not bind multicast addresses. In some platforms enabling using the TUN interface "
					+ "\n      for (Open)VPN causes this. If this is your situation try configure (Open)VPN to use TAP instead.";

			System.out.println(lastStartError);
			return false;
		}

		// We count ourselves as a participant that is present

		// The "lookup_xxx" operations not only retrieve the builtin data-readers but also
		// activate the cacheing of discovered types. To save resources discovered types
		// are only saved in the builtin reader cache which is only active after calling
		// the corresponding "lookup_xxx" operation 
		participantsDR = (ParticipantBuiltinTopicDataDataReader)
				participant.get_builtin_subscriber().lookup_datareader("DCPSParticipant");

		publicationsDR = (PublicationBuiltinTopicDataDataReader) 
				participant.get_builtin_subscriber().lookup_datareader("DCPSPublication");

		subscriptionsDR = (SubscriptionBuiltinTopicDataDataReader) 
				participant.get_builtin_subscriber().lookup_datareader("DCPSSubscription");

		// Create a WaitSet object that can be used to block the calling thread until there is
		// discovery data to read. This avoids having to poll and this use CPU continuously. 
		discoveryWaitSet = new WaitSet();

		// Attach the conditions that would wakeup the waitset. In this case the arrival of data on
		// any of the builting datareaders
		discoveryWaitSet.attach_condition(participantsDR.get_statuscondition());
		participantsDR.get_statuscondition().set_enabled_statuses(StatusKind.DATA_AVAILABLE_STATUS);

		discoveryWaitSet.attach_condition(publicationsDR.get_statuscondition());
		publicationsDR.get_statuscondition().set_enabled_statuses(StatusKind.DATA_AVAILABLE_STATUS);

		discoveryWaitSet.attach_condition(subscriptionsDR.get_statuscondition());
		subscriptionsDR.get_statuscondition().set_enabled_statuses(StatusKind.DATA_AVAILABLE_STATUS);

		activeConditionSeq = new ConditionSeq(MAX_ACTIVE_CONDITIONS);

		return true;
	}


	public void printDiscoveredParticipants() {
		ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
		SampleInfo info = new SampleInfo();;


		try {
			while ( true ) {          
				participantsDR.take_next_sample(participantData, info);
				//				participantsDR.read_next_sample(participantData, info);

				System.out.println("\n........... participante: "
						+ info.instance_handle.hashCode() + "  "
						+ participantData.participant_name.name);


				if ( info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE ) {
					//                   System.out.println("\n user data --=> " + participantData.user_data.value.toString());
					System.out.println("\n instance_handle --=> " 
							+ info.instance_handle.hashCode() + "  "
							+ info.instance_handle);

					System.out.println("Participant (New)" +
							" messageNum: " + info.reception_sequence_number.low +
							" name: \"" + participantData.participant_name.name + "\"" +
							" created at: " + info.source_timestamp +
							" detected at: " + info.reception_timestamp +
							" source sn: " + info.publication_sequence_number.low +
							" handle: " + info.instance_handle.toString() +
							" full details: " + participantData.toString());

					socketManager.getServer().getBroadcastOperations().sendEvent(
							"novo_participante",
							new Participante(info.instance_handle.hashCode(), participantData.participant_name.name));

				} else {
					String dissapearReason;
					if ( info.instance_state == InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE  ) {
						dissapearReason = "deleted";
					} else {
						dissapearReason = "lost connection";
					} 

					System.out.println("------------> " + info.instance_handle.hashCode() + " desapareceu!");

					socketManager.getServer().getBroadcastOperations().sendEvent(
							"participante_saiu",
							new Participante(info.instance_handle.hashCode()));

					if ( info.valid_data ) {
						System.out.println("Participant (Dissapeared - "+ dissapearReason +"):" +
								" messageNum: "  + info.reception_sequence_number.low +
								" name: \"" + participantData.participant_name.name + "\"" +
								" detected at: " + info.reception_timestamp +
								" source sn: " + info.publication_sequence_number.low +
								" handle: " + info.instance_handle.toString() +
								" full details: " + participantData.toString());



					}
					else {
						System.out.println("Participant (Dissapeared - "+ dissapearReason +"):" +
								" messageNum: "  + info.reception_sequence_number.low +
								" source sn: " + info.publication_sequence_number.low +
								" handle: " + info.instance_handle.toString() +
								" detected at: " + info.reception_timestamp );                     
					}
				}
			}

		} 
		catch (RETCODE_NO_DATA noData) {  } 

		finally {  }
	}


	public void printDiscoveredDataWriters() {
		PublicationBuiltinTopicData publicationData = new PublicationBuiltinTopicData();
		SampleInfo info = new SampleInfo();

		try {
			while ( true ) {
				publicationsDR.take_next_sample(publicationData, info);

//				if ( info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE ) {
//					System.out.println("DataWriter (New)" +
//							" messageNum: " + info.reception_sequence_number.low +
//							" name: \""  + publicationData.publication_name.name + "\"" +
//							" topic: " + publicationData.topic_name +
//							" type: "  + publicationData.type_name +
//							" created at: " + info.source_timestamp +
//							" detected at: " + info.reception_timestamp +
//							" handle: " + info.instance_handle.toString() +
//							" full details: " + publicationData.toString());
//
//				} else {
//					String dissapearReason;
//					if ( info.instance_state == InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE  ) {
//						dissapearReason = "deleted";
//					} else {
//						dissapearReason = "lost connection";
//					}    
//					if ( info.valid_data ) {
//						System.out.println("DataWriter (Dissapeared - "+ dissapearReason +"):" +
//								" messageNum: " + info.reception_sequence_number.low +
//								" name: \""  + publicationData.publication_name.name + "\"" +
//								" topic: " + publicationData.topic_name +
//								" type: "  + publicationData.type_name +
//								" created at: " + info.source_timestamp +
//								" detected at: " + info.reception_timestamp +
//								" handle: " + info.instance_handle.toString() +
//								" full details: " + publicationData.toString());
//					} else {
//						System.out.println("DataWriter (Dissapeared - "+ dissapearReason +"):" +
//								" messageNum: " + info.reception_sequence_number.low +
//								" handle: " + info.instance_handle.toString() +
//								" detected at: " + info.reception_timestamp );                       
//					}
//				}
			}
		}
		catch (RETCODE_NO_DATA noData) {    } 
		// catch (RETCODE_BAD_PARAMETER badParam) {   }
		finally {  }
	}

	public void printDiscoveredDataReaders() {
		SubscriptionBuiltinTopicData subscriptionData = new SubscriptionBuiltinTopicData();;
		SampleInfo info = new SampleInfo();;

		try {

			while ( true ) {              
				subscriptionsDR.take_next_sample(subscriptionData, info);

//				if ( info.instance_state == InstanceStateKind.ALIVE_INSTANCE_STATE ) {
//					System.out.println("DataReader (New)" +
//							" messageNum: " + info.reception_sequence_number.low +
//							" name: \""  + subscriptionData.subscription_name.name + "\"" +
//							" topic: " + subscriptionData.topic_name +
//							" type: "  + subscriptionData.type_name +
//							" created at: " + info.source_timestamp +
//							" detected at: " + info.reception_timestamp +
//							" handle: " + info.instance_handle.toString() +
//							" full details: " + subscriptionData.toString());
//
//				} else {
//					String dissapearReason;
//					if ( info.instance_state == InstanceStateKind.NOT_ALIVE_DISPOSED_INSTANCE_STATE  ) {
//						dissapearReason = "deleted";
//					} else {
//						dissapearReason = "lost connection";
//					}    
//					if ( info.valid_data ) {
//						System.out.println("DataReader (Dissapeared - "+ dissapearReason +"):" +
//								" messageNum: " + info.reception_sequence_number.low +
//								" name: \""  + subscriptionData.subscription_name.name + "\"" +
//								" topic: " + subscriptionData.topic_name +
//								" type: "  + subscriptionData.type_name +
//								" created at: " + info.source_timestamp +
//								" detected at: " + info.reception_timestamp +
//								" handle: " + info.instance_handle.toString() +
//								" full details: " + subscriptionData.toString());
//					} else {
//						System.out.println("DataReader (Dissapeared - "+ dissapearReason +"):" +
//								" messageNum: " + info.reception_sequence_number.low +
//								" handle: " + info.instance_handle.toString() +
//								" detected at: " + info.reception_timestamp );                       
//					}
//				}

			}
		}
		catch (RETCODE_NO_DATA noData) {  }
		// catch (RETCODE_BAD_PARAMETER badParam) {  } 
		finally {  }

	}

	public void waitForDiscoveryData() {
		Duration_t waitDuration = 
				new Duration_t(Duration_t.DURATION_INFINITE_SEC, Duration_t.DURATION_INFINITE_NSEC);

		System.out.println("waitForDiscoveryData");
		discoveryWaitSet.wait(activeConditionSeq, waitDuration);
	}

	public void printReceivedDiscoveredData() {
		System.out.println("printReceivedDiscoveredData");
		if ( participantsDR.get_statuscondition().get_trigger_value() ) {
			printDiscoveredParticipants();
		}
		if ( publicationsDR.get_statuscondition().get_trigger_value() ) {
			printDiscoveredDataWriters();
		}
		if ( subscriptionsDR.get_statuscondition().get_trigger_value() ) {
			printDiscoveredDataReaders();
		}

	}

	public static void main(String args[]) throws InterruptedException {

		String NDDSHOME = System.getenv("NDDSHOME");  
		String DYLD_LIBRARY_PATH = System.getenv("DYLD_LIBRARY_PATH");  
		System.out.println("NDDSHOME="+ NDDSHOME);  
		System.out.println("DYLD_LIBRARY_PATH="+ DYLD_LIBRARY_PATH);  
		int domainId = 0;

		MonitorDiscoveryInformation discoverySpy = new MonitorDiscoveryInformation();
		if ( !discoverySpy.start(domainId) ) {
			return;
		}

		while (true) {
			discoverySpy.waitForDiscoveryData();
			discoverySpy.printReceivedDiscoveredData();
		}
	}


	@Override
	public void run() {
		int domainId = 0;
		start(domainId);

		socketManager.server.addConnectListener(new ConnectListener() {

			@Override
			public void onConnect(SocketIOClient cliente) {
				System.out.println("cliente do browser conectou com IP " + cliente.getRemoteAddress().toString());
				InstanceHandleSeq ihs = new InstanceHandleSeq();
				participant.get_discovered_participants( ihs );
				System.out.println("\nParticipantes online:");
				
				for (Object part : ihs) {
					ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
					SampleInfo info = new SampleInfo();;
					System.out.println();
					participant.get_discovered_participant_data(participantData, (InstanceHandle_t) part);
					System.out.println(part.hashCode() + "  " + participantData.participant_name.name);

					socketManager.getServer().getBroadcastOperations().sendEvent(
							"novo_participante",
							new Participante(part.hashCode(), participantData.participant_name.name));
				}

			}
		});

		while (true) {
			waitForDiscoveryData();
			printReceivedDiscoveredData();
		}

	}
}