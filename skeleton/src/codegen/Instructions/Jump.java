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
                op = "jge";
                break;
            case GT:
                op = "jg";
                break;
            case LE:
                op = "jle";
                break;
            case LT:
                op = "jl";
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
