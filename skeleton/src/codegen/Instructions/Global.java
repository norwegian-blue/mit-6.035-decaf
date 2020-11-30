package codegen.Instructions;

/**
 * @author Nicola
 */
public class Global extends Exp {

    private final String name;
    private final int size;
    private final Exp offset;
    
    public Global(String name, int size) {
        this.name = name;
        this.size = size;
        this.offset = new Literal(0);
    }
    
    public Global(String name, Exp offset) {
        this.name = name;
        this.offset = offset;
        this.size = 0;
    }
    
    @Override
    public String toString() {
        return name + "[" + offset.toString() + "]";
    }
    
    @Override
    public String toCode() {
        return name + "(, " + offset.toCode() + ", 8)";
    }
    
    public String toAllocation() {
        return ".comm\t" + name + ", " + size + ", 8";
    }

}
