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
    
}
