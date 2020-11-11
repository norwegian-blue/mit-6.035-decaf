package semantic;

/**
 * @author Nicola
 */
public class ParameterDescriptor extends Descriptor {
    
    public ParameterDescriptor(String name, TypeDescriptor type) {
        super(name, type);
    }
    
    @Override
    public String toString() {
        return "[PAR] " + this.type.toString() + " " + this.name;
    }
       
}
