package semantic;

/**
 * @author Nicola
 */
public abstract class Descriptor {
    
    protected String name;
    protected TypeDescriptor type;
    
    public Descriptor(String name, TypeDescriptor type) {
        this.name = name;
        this.type = type;
    }
    
    public TypeDescriptor getType() {
        return this.type;
    }
    
    public boolean isMethod() {
        return false;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

}
