package prototipo;

public class Participante {

	int id;
	String name;
	
	public Participante(int hashCode, String name) {
		// TODO Auto-generated constructor stub
		this.id = hashCode;
		this.name = name;
	}
	public Participante(int hashCode) {
		this.id = hashCode;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String nome) {
		this.name = nome;
	}
	
}
