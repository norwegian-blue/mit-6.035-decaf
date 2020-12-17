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
    public String toString() {
        String str = "NODE CMP\t";
        str += " (" + cond.toString() + ")";
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
    
    @Override
    public IrExpression getExp() {
        return this.getCond();
    }
    
    @Override
    public void setExp(IrExpression exp) {
        this.setCond(exp);
    }
    
}
