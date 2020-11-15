package cfg;

import ir.Declaration.IrMemberDeclaration;

/**
 * @author Nicola
 */

public class CfgDeclaration extends CfgNode {
    
    private final IrMemberDeclaration decl;
    
    public CfgDeclaration(IrMemberDeclaration decl) {
        this.decl = decl;
    }
   
}
