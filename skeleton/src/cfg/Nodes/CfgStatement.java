package cfg.Nodes;

import ir.Statement.IrStatement;

/**
 * @author Nicola
 */

public class CfgStatement extends CfgNode {
    
    public CfgStatement(IrStatement stat) {
        this.nodeInstruction = stat;
    }
    
}
