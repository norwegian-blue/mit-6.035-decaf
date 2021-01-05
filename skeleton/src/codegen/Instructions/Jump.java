package codegen.Instructions;

import ir.Expression.IrBinaryExpression.BinaryOperator;

/**
 * @author Nicola
 */

public class Jump extends LIR {
    
    private final String destLabel;
    private BinaryOperator cond;
    
    public Jump(String destLabel, BinaryOperator cond) {
        this.destLabel = destLabel;
        this.cond = cond;
    }
    
    public Jump(String destLabel) {
        this.destLabel = destLabel;
        this.cond = null;
    }
        
    @Override
    public String toCode() {
        
        String op = "";
        
        if (cond == null) {
            // Unconditional jump
            op = "jmp";
        
        } else {
        
            // Conditional jump
            switch (cond) {
            case EQ:
                op = "je";
                break;
            case GE:
                op = "jae";
                break;
            case GT:
                op = "ja";
                break;
            case LE:
                op = "jbe";
                break;
            case LT:
                op = "jb";
                break;
            case NEQ:
                op = "jne";
                break;
            default:
                throw new Error("Unsupported condition");
            }
        }
        
        return "\t" + op + "\t" + this.destLabel;
    }
    
}
