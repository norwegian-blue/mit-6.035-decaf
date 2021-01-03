package codegen.Instructions;

/**
 * @author Nicola
 */
public class Push extends LIR {
    
    private Exp exp;
    private int size;
    
    public Push(Exp exp) {
        // TODO remove
        this.exp = exp;
        this.size = 8;
    }
    
    public Push(Exp exp, int size) {
        this.exp = exp;
        this.size = size;
    }
    
    @Override
    public String toCode() {
        return "\tpush" + Exp.getSuffix(size) + "\t" + exp.toCode();
    }
}
