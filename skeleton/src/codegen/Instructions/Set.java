package codegen.Instructions;

import ir.Expression.IrBinaryExpression.BinaryOperator;

/**
 * @author Nicola 
 */

public class Set extends LIR {
    
    private final BinaryOperator cond;
    private Exp dest;
        
    public Set(BinaryOperator cond, Exp dest) {
        this.dest = dest;
        this.cond = cond;
    }
    
    @Override
    public String toCode() {
        
        String sfx;
        
        switch (cond) {
        case GT:
            sfx = "g";
            break;
        case LT:
            sfx = "l";
            break;
        case GE:
            sfx = "ge";
            break;
        case LE:
            sfx = "le";
            break;
        case EQ:
            sfx = "e";
            break;
        case NEQ:
            sfx = "ne";
            break;
        default:
            throw new Error("Undefined comparison operator");
        }
        
        return "\tset" + sfx + "\t" + this.dest.toCode();
    }

}
