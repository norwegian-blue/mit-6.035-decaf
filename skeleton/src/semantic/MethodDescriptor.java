package semantic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicola
 */
public class MethodDescriptor extends Descriptor {

    private List<ParameterDescriptor> parameters;
    private List<LocalDescriptor> locals;
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters) {
        super(name, returnType);
        this.parameters = parameters;
        this.locals = new ArrayList<LocalDescriptor>();
    }
    
    public MethodDescriptor(String name, TypeDescriptor returnType, List<ParameterDescriptor> parameters, List<LocalDescriptor> locals) {
        super(name, returnType);
        this.parameters = parameters;
        this.locals = locals;
    }
    
    @Override
    public boolean isMethod() {
        return true;
    }
    
    public List<ParameterDescriptor> getPars() {
        return this.parameters;
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
    
    public List<LocalDescriptor> getLocals() {
        return locals;
    }
    
    public void addLocal(LocalDescriptor local) {
        this.locals.add(local);
    }
}
