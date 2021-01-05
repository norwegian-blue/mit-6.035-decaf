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
        String suffix = "q";
        if (exp.getSuffix().equals("b")) {
            suffix = "w";
        }
        return "\tpush" + suffix + "\t" + exp.toCode();
    }
}
