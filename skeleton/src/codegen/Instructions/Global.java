package codegen.Instructions;

/**
 * @author Nicola
 */
public class Global extends Location {

    private final String name;
    private final int lenght;
    private Exp offset;
    
    public Global(String name, int lenght, int elSize) {
        this.name = name;
        this.size = elSize;
        this.lenght = lenght;
        this.offset = new Literal(0);
    }
    
    public Global(Global glb) {
        this.name = glb.name;
        this.size = glb.size;
        this.lenght= glb.lenght;
        this.offset = glb.offset;
    }
    
    public void setOffset(Exp offset) {
        this.offset = offset;
    }
    
    public int getLen() {
        return this.lenght;
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
        return ".comm\t" + name + ", " + lenght*size + ", 8";
    }

}
