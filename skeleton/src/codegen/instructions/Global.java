package codegen.instructions;

/**
 * @author Nicola
 */
public class Global extends Exp {

    private final String name;
    private final int size;
    
    public Global(String name, int size) {
        this.name = name;
        this.size = size;
    }
    
    @Override
    public String toCode() {
        return name + "(%rip)";
    }
    
    public String toAllocation() {
        return ".comm\t" + name + ", " + size;
    }

}
