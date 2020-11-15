package cfg;

import ir.Expression.IrExpression;

/**
 * @author Nicola
 */

public class CfgBranch extends CfgNode {

    private IrExpression cond;
    
    public CfgBranch(IrExpression cond) {
        this.cond = cond;
    }
    
}
