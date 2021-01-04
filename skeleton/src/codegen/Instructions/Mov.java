package codegen.Instructions;

/**
 * @author Nicola
 */
public class Mov extends LIR {
    
    protected final Exp src;
    protected final Exp dest;
    
    public Mov(Exp src, Exp dest) {
        this.src = src;
        this.dest = dest;
    }
    
    @Override
    public String toString() {
        return "move\t" + src.toString() + ", " + dest.toString();
    }

    @Override
    public String toCode() {
        return "\tmov" + src.getSuffix() + "\t" + src.toCode() + ", " + dest.toCode();
    }
        
}
