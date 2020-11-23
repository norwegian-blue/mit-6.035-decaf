package cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.Declaration.*;

/**
 * @author Nicola
 */

public class ProgramCFG {

    private List<IrFieldDeclaration> globals;
    private Map<String, MethodCFG> methods;
    
    public ProgramCFG(IrClassDeclaration program) {
        this.globals = program.getFields();
        this.methods = new HashMap<String, MethodCFG>();
        
        for (IrMethodDeclaration method : program.getMethods()) {
            MethodCFG CFG = CFGCreator.BuildMethodCFG(method);
            CFG.removeNoOps();
            this.methods.put(method.getId(), CFG);            
        }
    }
    
    public void flatten() {
        for (String methodName : methods.keySet()) {
            methods.get(methodName).flatten();
        }
    }
    
    public void blockify() {
        for (String method: methods.keySet()) {
            methods.put(method, methods.get(method).blockify());
        }
    }
    
    @Override 
    public String toString() {
        String graph = "";
        for (String method : methods.keySet()) {
            graph += method + ":\n" + methods.get(method).toString() + "\n\n";
        }
        return graph;
    }
    
}
