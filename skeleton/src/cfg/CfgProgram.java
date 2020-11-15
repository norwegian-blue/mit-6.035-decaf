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
    private Map<String, CFG> methods;
    
    public CfgProgram(IrClassDeclaration program) {
        this.globals = program.getFields();
        this.methods = new HashMap<String, CFG>();
        
        for (IrMethodDeclaration method : program.getMethods()) {
            CFG methodCFG = method.accept(new CfgCreator());
            methodCFG.removeNoOps();
            this.methods.put(method.getId(), methodCFG);            
        }
    }
    
    public void flatten() {
        //TODO implement tree flattener --> create temporary variables
    }
    
}
