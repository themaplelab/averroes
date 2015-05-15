package averroes.soot;

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
		this(0, "r");
	}

	public LocalVariableNumberer(int number, String letter) {
		this.number = number;
		this.letter = letter;
	}

	public String next() {
		return letter + number++;
	}

}
