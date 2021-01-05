package codegen.Instructions;

import ir.Expression.IrBinaryExpression.BinaryOperator;

/**
 * @author Nicola
 */
public class CMov extends LIR {

    private final BinaryOperator cond;
    private Register src;
    private Register dest;
        
    public CMov(BinaryOperator cond, Register src, Register dest) {
        this.src = src;
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
        
        if (src.getSuffix().equals("b") || dest.getSuffix().equals("b")) {
            src = new Register(src, 2);
            dest = new Register(dest, 2);
        }
        
        return "\tcmov" + sfx + "\t" + this.src.toCode() + ", " + this.dest.toCode();
    }
}
