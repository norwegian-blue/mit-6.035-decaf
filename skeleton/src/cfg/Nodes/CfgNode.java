package cfg.Nodes;

import java.util.HashSet;
import java.util.Set;

import ir.Ir;

/** 
 * @author Nicola
 */

public abstract class CfgNode {
    
    protected Set<CfgNode> parentNodes = new HashSet<CfgNode>();
    protected CfgNode trueBranch;
    protected CfgNode falseBranch;
    protected Ir nodeInstruction;  
    
    protected void addParentNode(CfgNode parent) {
        this.parentNodes.add(parent);
    }
    
    public void setParents(Set<CfgNode> parents) {
        this.parentNodes = parents;
    }
    
    public Set<CfgNode> getParents() {
        return this.parentNodes;
    }
    
    public CfgNode getTrueBranch() {
        return this.trueBranch;
    }
    
    public CfgNode getFalseBranch() {
        return this.falseBranch;
    }
    
    public void setTrueBranch(CfgNode trueBranch) {
        this.trueBranch = trueBranch;
        trueBranch.addParentNode(this);
    }
    
    public void setFalseBranch(CfgNode falseBranch) {
        this.falseBranch = falseBranch;
        falseBranch.addParentNode(this);
    }
    
    public void concatenate(CfgNode node) {
        this.trueBranch = node;
        this.falseBranch = node;
        node.addParentNode(this);
    }
    
    public boolean isNoOp() {
        return false;
    }
    
    public boolean isBranch() {
        return false;
    }
    
    @Override
    public String toString() {
        return "NODE:\t" + this.nodeInstruction.toString();
    }
    
    public boolean hasNext() {
        return (this.trueBranch != null) && (this.falseBranch != null);
    }
}
