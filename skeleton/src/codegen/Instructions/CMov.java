package codegen.Instructions;

/**
 * @author Nicola
 */
public class CMov extends Mov {

    private final Condition cond;
    
    public static enum Condition {
        GT,
        LT,
        GE,
        LE,
        EQ, 
        NE
    }
        
    
    public CMov(Condition cond, Exp src, Exp dest) {
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
        case NE:
            sfx = "ne";
            break;
        default:
            throw new Error("Undefined condition");
        }
        
        return "cmov" + sfx + "\t" + this.src.toCode() + ", " + this.dest.toCode();
    }
}
