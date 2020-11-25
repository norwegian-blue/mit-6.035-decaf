package codegen.instructions;

/**
 * @author Nicola
 */

public class Local extends Exp {

    private int offset;
    
    public Local(int offset) {
        this.offset = offset;
    }
    
    @Override
    public String toString() {
        return "BP + " + offset;
    }
    
    @Override
    public String toCode() {
        return "-" + offset + "(%rbp)";
    }
    
}
