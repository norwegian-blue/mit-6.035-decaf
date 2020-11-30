package semantic;

/**
 * @author Nicola
 */
public class FieldDescriptor extends Descriptor {
    
    public FieldDescriptor(String name, TypeDescriptor type) {
        super(name, type);
    }
    
    @Override
    public String toString() {
        return "[FIELD] " + this.type.toString() + " " + this.name; 
    }  
    
    @Override
    public boolean isGlobal() {
        return true;
    }
}
