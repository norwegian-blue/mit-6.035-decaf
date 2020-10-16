package semantic;

/**
 * @author Nicola
 */
public class ClassDescriptor extends Descriptor {
    
    private final FieldDescriptor[] fields;
    private final MethodDescriptor[] methods;
    
    public ClassDescriptor(String name, FieldDescriptor[] fields, MethodDescriptor[] methods) {
        super(name);
        this.fields = fields;
        this.methods = methods;
    }
    
}
