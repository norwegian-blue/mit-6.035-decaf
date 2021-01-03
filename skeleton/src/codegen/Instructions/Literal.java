package codegen.Instructions;

/**
 * @author Nicola
 */
public class Literal extends Exp {

    private final int value;
    private final int size;
    
    public Literal(int value) {
        // TODO remove
        this.value = value;
        this.size = 8;
    }
    
    public Literal(int value, int size) {
        this.value = value;
        this.size = size;
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
