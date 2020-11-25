package codegen.Instructions;

/**
 * @author Nicola
 */
public class Literal extends Exp {

    private final int value;
    
    public Literal(int value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "" + this.value;
    }

    @Override
    public String toCode() {
        return "$" + value;
    }
}
