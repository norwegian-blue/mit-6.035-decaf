package semantic;

import java.util.List;

/**
 * @author Nicola
 */
public class MethodDescriptor extends Descriptor {

    private List<ParameterDescriptor> parameters;
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters) {
        super(name, returnType);
        this.parameters = parameters;
    }
    
    @Override
    public boolean isMethod() {
        return true;
    }
    
    @Override
    public String toString() {
        String str = "[METHOD] " + this.type.toString() + " (";
        for (ParameterDescriptor par : parameters) {
            str += par.toString() + ", ";
        }
        if (!parameters.isEmpty()) {
            str = str.substring(0, str.length()-2);
        }
        return str + ")";
    }       
}
