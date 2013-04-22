package ca.uwaterloo.averroes.soot;

/**
 * A numberer for local variables in a the Jimple body of a method. This is extensively by {@link CodeGenerator}.
 * 
 * @author karim
 * 
 */
public class LocalVariableNumberer {

	private int number;
	private String letter;

	public LocalVariableNumberer() {
		number = 0;
		letter = "r";
	}

	public LocalVariableNumberer(String letter) {
		number = 0;
		this.letter = letter;
	}

	public String next() {
		return letter + number++;
	}

}
