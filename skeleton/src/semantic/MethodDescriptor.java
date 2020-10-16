package semantic;

/**
 * @author Nicola
 */
public class MethodDescriptor extends Descriptor {
    
    private TypeDescriptor returnType;
    private ParameterDescriptor[] parameters;
    private LocalDescriptor[] locals;
    
    public MethodDescriptor(String name, TypeDescriptor returnType, ParameterDescriptor[] parameters, LocalDescriptor[] locals) {
        super(name);
        this.returnType = returnType;
        this.parameters = parameters;
        this.locals = locals;
    }

}
