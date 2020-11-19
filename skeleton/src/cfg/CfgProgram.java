package cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.Declaration.*;

/**
 * @author Nicola
 */

public class CfgProgram {

    private List<IrFieldDeclaration> globals;
    private Map<String, MethodCFG> methods;
    
    public CfgProgram(IrClassDeclaration program) {
        this.globals = program.getFields();
        this.methods = new HashMap<String, MethodCFG>();
        
        for (IrMethodDeclaration method : program.getMethods()) {
            MethodCFG CFG = CfgCreator.BuildMethodCFG(method);
            CFG.removeNoOps();
            this.methods.put(method.getId(), CFG);            
        }
    }
    
    public void flatten() {
        //TODO implement tree flattener --> create temporary variables
    }
    
}
