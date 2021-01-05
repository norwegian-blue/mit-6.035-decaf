package codegen.Instructions;

/**
 * @author Nicola
 */
public class BinOp extends LIR {
    
    private Exp lhs;
    private Exp rhs;
    private String operation;
        
    public BinOp(String operation, Exp lhs, Exp rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operation = operation;
        
    }
   
    @Override
    public String toCode() {
        String suffix = getSuffix(lhs, rhs);
        if (suffix.equals("b")) {
            if (lhs.isReg()) lhs.size = 1;
            if (rhs.isReg()) rhs.size = 1;
        }
        
        return "\t" + operation + suffix + "\t" + lhs.toCode() + ", " + rhs.toCode();            
    }

}
