package semantic;

/**
 * @author Nicola
 */
public class ParameterDescriptor extends Descriptor {
    
    private TypeDescriptor type;
    
    public ParameterDescriptor(String name, TypeDescriptor type) {
        super(name);
        this.type = type;
    }
       
}
