package semantic;

/**
 * @author Nicola
 */
public class FieldDescriptor extends Descriptor {
    
    private TypeDescriptor type;
    
    public FieldDescriptor(String name, TypeDescriptor type) {
        super(name);
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "[FIELD] " + type.toString() + " " + this.name; 
    }    
}
