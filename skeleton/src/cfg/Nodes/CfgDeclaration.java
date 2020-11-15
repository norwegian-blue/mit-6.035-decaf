package cfg.Nodes;

import ir.Declaration.IrMemberDeclaration;

/**
 * @author Nicola
 */

public class CfgDeclaration extends CfgNode {
        
    public CfgDeclaration(IrMemberDeclaration decl) {
        this.nodeInstruction = decl;
    }
   
}
