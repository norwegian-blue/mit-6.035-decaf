package semantic;

import java.util.List;

/**
 * @author Nicola
 */
public class MethodDescriptor extends Descriptor {
    
    private TypeDescriptor returnType;
    private List<ParameterDescriptor> parameters;
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters) {
        super(name);
        this.returnType = returnType;
        this.parameters = parameters;
    }
    
    @Override
    public String toString() {
        String str = "[METHOD] " + returnType.toString() + " (";
        for (ParameterDescriptor par : parameters) {
            str += par.toString() + ", ";
        }
        str = str.substring(0, str.length()-2) + ")";
        return str;
    }       
}
