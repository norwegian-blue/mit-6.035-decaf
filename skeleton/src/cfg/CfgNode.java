package cfg;

import java.util.HashSet;
import java.util.Set;

import ir.Statement.IrStatement;

/** 
 * @author Nicola
 */

public abstract class CfgNode {
    
    protected Set<CfgNode> parentNodes = new HashSet<CfgNode>();
    protected CfgNode trueBranch;
    protected CfgNode falseBranch;
    protected IrStatement nodeInstruction;  
    
    protected void addParentNode(CfgNode parent) {
        this.parentNodes.add(parent);
    }
    
    protected void setTrueBranch(CfgNode trueBranch) {
        this.trueBranch = trueBranch;
        trueBranch.addParentNode(this);
    }
    
    protected void setFalseBranch(CfgNode falseBranch) {
        this.falseBranch = falseBranch;
        falseBranch.addParentNode(this);
    }
    
    public static void concatenate(CfgNode node1, CfgNode node2) {
        node1.trueBranch = node2;
        node1.falseBranch = node2;
        node2.addParentNode(node1);
    }

}
