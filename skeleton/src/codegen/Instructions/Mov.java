package codegen.Instructions;

/**
 * @author Nicola
 */
public class Mov extends LIR {
    
    protected Exp src;
    protected Exp dest;
    
    public Mov(Exp src, Exp dest) {
        this.src = src;
        this.dest = dest;
    }
    
    @Override
    public String toCode() {
        
        String suffix = getSuffix(src, dest);
        
        if (suffix.equals("b") && dest.isReg()) {
            dest = new Register((Register) dest, 1);
        }
        
        if (suffix.equals("b") && src.isReg()) {
            src = new Register((Register) src, 1);
        }
        
        return "\tmov" + suffix + "\t" + src.toCode() + ", " + dest.toCode();
    }
        
}
