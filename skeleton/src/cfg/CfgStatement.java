package cfg;

import ir.Statement.IrStatement;

/**
 * @author Nicola
 */

public class CfgStatement extends CfgNode {

    private final IrStatement stat;
    
    public CfgStatement(IrStatement stat) {
        this.stat = stat;
    }
    
}
