package prototipo;

public class MensagemGenerica {

	String string1;
	String string2;
	int int1;
	
	public MensagemGenerica(int int1) {
		super();
		this.int1 = int1;
	}
	public MensagemGenerica(String string1) {
		super();
		this.string1 = string1;
	}
	public MensagemGenerica(int i, String s) {
		this.int1 = i;
		this.string1 = s;
	}
	public String getString1() {
		return string1;
	}
	public void setString1(String string1) {
		this.string1 = string1;
	}
	public String getString2() {
		return string2;
	}
	public void setString2(String string2) {
		this.string2 = string2;
	}
	public int getInt1() {
		return int1;
	}
	public void setInt1(int int1) {
		this.int1 = int1;
	}
	
	
	
}
