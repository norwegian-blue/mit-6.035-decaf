package cfg.Nodes;

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
    public String nodeString() {
        String str = "NODE STMT";
        if (printIr) {
            str += ": " + stat.toString();
        }
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
    
}
