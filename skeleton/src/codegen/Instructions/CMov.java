package codegen.Instructions;

import ir.Expression.IrBinaryExpression.BinaryOperator;

/**
 * @author Nicola
 */
public class CMov extends Mov {

    private final BinaryOperator cond;
        
    public CMov(BinaryOperator cond, Exp src, Exp dest) {
        super(src, dest);
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
        
        return "\tcmov" + sfx + "\t" + this.src.toCode() + ", " + this.dest.toCode();
    }
}
