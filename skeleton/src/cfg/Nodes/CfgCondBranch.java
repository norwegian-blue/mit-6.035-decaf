package cfg.Nodes;

import ir.Expression.IrExpression;

/**
 * @author Nicola
 */

public class CfgCondBranch extends CfgBranchNode {
    
    private IrExpression cond;
    
    public CfgCondBranch(IrExpression cond) {
        super();
        this.cond = cond;
    }
    
    @Override
    public String nodeString() {
        return "NODE CMP";
    }
}
