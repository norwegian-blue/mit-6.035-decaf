package cfg;

import java.util.ArrayList;
import java.util.List;

import cfg.Nodes.*;
import ir.Declaration.IrVariableDeclaration;

/**
 * @author Nicola
 */

public class MethodCFG extends CFG {

    private List<IrVariableDeclaration> locals = new ArrayList<IrVariableDeclaration>();
    
    public MethodCFG(Node root) {
        super(root);
    }
    
    public void addLocal(IrVariableDeclaration local) {
        this.locals.add(local);
    }
    
}
