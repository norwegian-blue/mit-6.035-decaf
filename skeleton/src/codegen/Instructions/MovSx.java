package codegen.Instructions;

/**
 * @author Nicola
 */

public class MovSx extends Mov {
    
    public MovSx(Exp src, Exp dest) {
        super(src, dest);
    }
    
    @Override
    public String toCode() {
        if (src.getSuffix().equals("b") && !src.isLiteral()) {
            return "\tmovsbq\t" + src.toCode() + ", " + dest.toCode();
        } else {
            return "\tmovq\t" + src.toCode() + ", " + dest.toCode();
        }
    }

}
