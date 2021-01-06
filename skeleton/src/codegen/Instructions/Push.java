package codegen.Instructions;

/**
 * @author Nicola
 */
public class Push extends LIR {
    
    private Exp exp;
    private String suffix;
    
    public Push(Exp exp) {
        this.exp = exp;
        this.suffix = exp.getSuffix();
    }
    
    @Override
    public String toCode() {
        if (suffix.equals("b")) {
            suffix = "w";
            if (exp.isReg()) {
                exp = new Register((Register) exp, 2);
            }
        }
        return "\tpush" + suffix + "\t" + exp.toCode();
    }
}
