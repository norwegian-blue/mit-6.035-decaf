package cfg.Nodes;

import ir.Expression.IrExpression;

/**
 * @author Nicola
 */

public class CfgBranch extends CfgNode {
    
    public CfgBranch(IrExpression cond) {
        this.nodeInstruction = cond;
    }
    
    @Override
    public boolean isBranch() {
        return true;
    }
    
}
