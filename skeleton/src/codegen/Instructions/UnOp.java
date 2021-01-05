package codegen.Instructions;

/**
 * @author Nicola
 */
public class UnOp extends LIR {
    
    private Exp term;
    private String operation;
        
    public UnOp(String operation, Exp term) {
        this.term = term;
        this.operation = operation;
        
    }
   
    @Override
    public String toCode() {
        return "\t" + operation + term.getSuffix() + "\t" + term.toCode();            
    }

}
