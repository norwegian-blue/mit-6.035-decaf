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
        String str = "NODE CMP";
        if (printIr) {
            str += " (" + cond.toString() + ")";
        }
        return str;
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.visit(this);
    }
    
    public IrExpression getCond() {
        return this.cond;
    }
    
    public void setCond(IrExpression newCond) {
        this.cond = newCond;
    }
    
}
