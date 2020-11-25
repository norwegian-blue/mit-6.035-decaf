package codegen.instructions;

/**
 * @author Nicola
 */

public class Binop extends LIR {
    
    private static Exp lhs;
    private static Exp rhs;
    private static BinOperator operator;
    
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
    public String toString() {
        // TODO 
        return null;
    }

    @Override
    public String toCode() {
        // TODO Auto-generated method stub
        return null;
    }

}
