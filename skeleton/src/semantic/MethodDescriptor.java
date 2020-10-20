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
    
    @Override
    public String toString() {
        String str = "[METHOD] " + returnType.toString() + " (";
        for (ParameterDescriptor par : parameters) {
            str += par.toString();
        }
        str += ") {";
        for (LocalDescriptor local : locals) {
            str += local.toString();
        }
        str += "}";
        return str;
    }       
}
