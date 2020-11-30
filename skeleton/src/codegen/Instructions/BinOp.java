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
    public String toString() {
        return operation + "\t" + lhs.toString() + ", " + rhs.toString();
    }
   
    @Override
    public String toCode() {
        return "\t" + operation + "\t" + lhs.toCode() + ", " + rhs.toCode();            
    }

}
