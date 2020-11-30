package codegen.Instructions;

/**
 * @author Nicola
 */
public class Push extends LIR {
    
    private Exp exp;
    
    public Push(Exp exp) {
        this.exp = exp;
    }
    
    @Override
    public String toCode() {
        return "\tpushq\t" + exp.toCode();
    }
}
