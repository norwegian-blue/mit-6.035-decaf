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
       

}
