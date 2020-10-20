package semantic;

/**
 * @author Nicola
 */
public class LocalDescriptor extends Descriptor {
    
    private TypeDescriptor type;
    
    public LocalDescriptor(String name, TypeDescriptor type) {
        super(name);
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "[LOCAL] " + type.toString();
    }   
}
