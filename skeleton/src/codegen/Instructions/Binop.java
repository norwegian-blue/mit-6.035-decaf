package codegen.Instructions;

/**
 * @author Nicola
 */

public class Binop extends LIR {
    
    private Exp lhs;
    private Exp rhs;
    private BinOperator operator;
    
    public enum BinOperator {
        PLUS,
        MINUS
    }
    
    public Binop(BinOperator operator, Exp lhs, Exp rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.operator = operator;
        
    }
   
    @Override
    public String toCode() {
        return "\tadd\t" + lhs.toCode() + ", " + rhs.toCode();
    }

}
