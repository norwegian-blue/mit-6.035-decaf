package cfg.Nodes;

import ir.Expression.IrExpression;
import ir.Statement.IrAssignment;
import ir.Statement.IrStatement;

/**
 * @author Nicola
 */

public class CfgStatement extends CfgLineNode {
    
    private IrStatement stat;
    
    public CfgStatement(IrStatement stat) {
        super();
        this.stat = stat;
    }
    
    @Override
    public String toString() {
        String str = "NODE STMT";
        str += "\t" + stat.toString();
        return str;
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.visit(this);
    }
    
    public IrStatement getStatement() {
        return this.stat;
    }
    
    public void setStatement(IrStatement newStat) {
        this.stat = newStat;
    }
    
    @Override
    public void setExp(IrExpression exp) {
        if (this.getStatement().isAssignment()) {
            ((IrAssignment)this.getStatement()).setExpression(exp);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override 
    public IrExpression getExp() {
        if (this.getStatement().isAssignment()) {
            return ((IrAssignment)this.getStatement()).getExpression();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
}
