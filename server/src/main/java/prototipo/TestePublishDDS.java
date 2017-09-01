package prototipo;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.DomainParticipantConfigParams_t;

public class TestePublishDDS {

	static HelloPublisher pub;

	public TestePublishDDS() {

	}

	public TestePublishDDS(HelloPublisher publisher) {
		pub = publisher;
	}

	int x = 10;
	private DomainParticipant participant;
	
	
	
	public void iniciar() {
		
		if (this.participant == null) {
			
//			this.participant = DomainParticipantFactory.get_instance()
//					.create_participant_from_config(
//							"MyParticipantLibrary::Participante1")
//					;

			DomainParticipantConfigParams_t conf = new DomainParticipantConfigParams_t();
			conf.participant_name = "Beltrano";
			
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

		

		while (x > 0) {

			pub.escrever("fulano", "ciclano", "olá!");

			try {
				Thread.sleep(2000);
//				pub.dispose();
//				break;

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			x--;
		
		}

	}


	public static void main(String[] args) {

		HelloPublisher publisher = new HelloPublisher();
		publisher.publisherStart(0);
		new TestePublishDDS(publisher).iniciar();

	}

}
