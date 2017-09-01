// ****************************************************************************
//         (c) Copyright, Real-Time Innovations, All rights reserved.       
//                                                                          
//         Permission to modify and use for internal purposes granted.      
// This software is provided "as is", without warranty, express or implied. 
//                                                                          
// ****************************************************************************

package prototipo;


import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.builtin.ParticipantBuiltinTopicData;
import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;


public class HelloPublisher extends Thread {

	DomainParticipant participant;
	DomainParticipant participant2;
	DynamicDataWriter dynamicWriter;
	DynamicData instance;
	int count;
	
	public HelloPublisher(Principal principal) {
	}

	public HelloPublisher() {
		count = 0;
		criaParticipante();
	}

	public HelloPublisher(DomainParticipant p) {
		count = 0;
		this.participant = p;
	}

	public void run() {
		publisherStart(0);
		//		dispose();
	}

	
	
	
	protected void dispose() {                

		if (this.dynamicWriter != null && instance != null) {                      
			this.dynamicWriter.delete_data(this.instance);
		}            

		if (this.participant != null) {
			this.participant.delete_contained_entities();
			this.dynamicWriter = null;

			DomainParticipantFactory.get_instance().delete_participant(
					this.participant);                
		}

		DomainParticipantFactory.finalize_instance();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void criaParticipante() {
		if (this.participant == null) {
		this.participant = DomainParticipantFactory.get_instance()
				.create_participant_from_config(
						"MyParticipantLibrary::Participante1")
				;


		if (this.participant == null) {
			System.out.println("! Unable to create DDS domain participant");
			return;
		}

	}
	}
	
	
	public void publisherStart(
			int sampleCount) {


		if (this.dynamicWriter == null) {
			System.out.println("\n Criando dynamicWriter...");
			
			this.dynamicWriter = (DynamicDataWriter) this.participant.lookup_datawriter_by_name(
					"MyPublisher::HelloWorldWriter");
			
			if (this.dynamicWriter == null) {
				System.out.println("! Unable to get DDS dynamic data writer");
				return;
			}
		}

		if (this.instance == null) {
			System.out.println("\n Criando instance...");

			this.instance = this.dynamicWriter.create_data(DynamicData.PROPERTY_DEFAULT);
			System.out.println("      aaaaaaaaaaaaaaa" + this.instance);

			if (this.instance == null) {
				System.out.println("! Unable to create DDS dynamic data instance");
				return;
			}
		}


	}


	public void escrever(String from, String to, String mensagem) {
		
//		ParticipantBuiltinTopicData participantData = new ParticipantBuiltinTopicData();
//		participant.get_discovered_participant_data(participantData, participant.get_instance_handle());
//		System.out.println("\n### " + participantData.participant_name.name);
		
		System.out.println("\nWriting to DDS... ");
		System.out.println("De: " + Integer.toString(participant.get_instance_handle().hashCode()) + "\nPara: " + to + "\nMensagem: " + mensagem);
		/*
		 * Set the data fields
		 */


		
		this.instance.set_string(		
				"from",
				DynamicData.MEMBER_ID_UNSPECIFIED,
				Integer.toString(participant.get_instance_handle().hashCode())
				);
		this.instance.set_string(
				"to",
				DynamicData.MEMBER_ID_UNSPECIFIED,
				to);
		this.instance.set_string("message",
				DynamicData.MEMBER_ID_UNSPECIFIED,
				mensagem);

		this.instance.set_int("count",
				DynamicData.MEMBER_ID_UNSPECIFIED,
				count);

		try {
			this.dynamicWriter.write(						// write to DDS
					this.instance, 
					InstanceHandle_t.HANDLE_NIL);
			count++;
		}
		catch (RETCODE_ERROR e) {
			System.out.println("! Write error:"
					+ e.getMessage());
			return;
		}


	}

	public static void main(String[] argv) {
		int sampleCount = 0; // infinite loop

		if (argv.length >= 1) {
			sampleCount = Integer.parseInt(argv[0]);
		}

		HelloPublisher publisher = new HelloPublisher();
		publisher.publisherStart(sampleCount);
		publisher.dispose();
	}
}
