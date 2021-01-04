package codegen.Instructions;

/**
 * @author Nicola
 */
public class Literal extends Exp {

    private final int value;
        
    public Literal(int value) {
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
    
    @Override 
    public boolean isLiteral() {
        return true;
    }
    
    @Override 
    public boolean equals(Object thatObject) {
        if (!(thatObject instanceof Literal)) {
            return false;
        } 
        Literal that = (Literal) thatObject;
        return that.value == this.value && that.size == this.size;
    }
}
