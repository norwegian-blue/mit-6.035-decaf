package codegen.Instructions;

/**
 * @author Nicola
 */
public class Mov extends LIR {
    
    protected final Exp src;
    protected final Exp dest;
    protected final int size;
    
    public Mov(Exp src, Exp dest) {
        // TODO remove for size
        this.src = src;
        this.dest = dest;
        this.size = 8;
    }
    
    public Mov(Exp src, Exp dest, int size) {
        this.src = src;
        this.dest = dest;
        this.size = size;
    }
    
    @Override
    public String toString() {
        return "move\t" + src.toString() + ", " + dest.toString();
    }

    @Override
    public String toCode() {
        return "\tmov" + Exp.getSuffix(size) + "\t" + src.toCode() + ", " + dest.toCode();
    }
        
}
