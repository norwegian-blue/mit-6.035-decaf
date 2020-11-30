package codegen.Instructions;

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
        String sign = (offset>0) ? "+" : "";
        return "BP" + sign + offset;
    }
    
    @Override
    public String toCode() {
        return offset + "(%rbp)";
    }
    
}
