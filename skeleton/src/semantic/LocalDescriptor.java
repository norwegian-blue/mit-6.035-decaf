package semantic;

/**
 * @author Nicola
 */
public class LocalDescriptor extends Descriptor {
    
    public LocalDescriptor(String name, TypeDescriptor type) {
        super(name, type);
    }
    
    @Override
    public String toString() {
        return "[LOCAL] " + this.type.toString() + " " + this.name;
    }   
}
