package codegen.Instructions;

/**
 * @author Nicola
 */
public class Global extends Location {

    private final String name;
    private final int lenght;
    private final int elSize;
    private final Exp offset;
    
    public Global(String name, int lenght, int elSize) {
        this.name = name;
        this.elSize = elSize;
        this.lenght = lenght;
        this.offset = new Literal(0);
    }
    
    // Global array
    public Global(String name, Exp offset) {
        this.name = name;
        this.offset = offset;
        this.elSize = 0;
        this.lenght = 0;
    }
    
    // Global scalar
    public Global(String name) {
        this.name = name;
        this.offset = null;
        this.elSize = 0;
        this.lenght = 0;
    }
    
    @Override
    public String toString() {
        return name + "[" + offset.toString() + "]";
    }
    
    @Override
    public String toCode() {
        if (this.offset == null) {
            return name + "(%rip)";
        } else {
            return name + "(, " + offset.toCode() + ", 8)";
        }
    }
    
    public String toAllocation() {
        return ".comm\t" + name + ", " + lenght*elSize + ", 8";
    }

}
