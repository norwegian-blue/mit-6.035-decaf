package cfg.Nodes;

import java.util.HashSet;
import java.util.Set;

import ir.Expression.IrExpression;
import ir.Expression.IrIdentifier;

/**
 * @author Nicola
 */

public abstract class Node {
    
    private boolean visited = false;
    
    protected Set<Node> parentNodes = new HashSet<Node>();
    protected Node childNodes[];
    private Node parentBlock = null;
    protected Set<IrIdentifier> liveVars = new HashSet<IrIdentifier>();
        
    public void addParentNode(Node parent) {
        this.parentNodes.add(parent);
    }
    
    public void setParents(Set<Node> parents) {
       this.parentNodes = parents;
    }
    
    public void removeParentNode(Node parent) {
        if (parentNodes.contains(parent)) {
            parentNodes.remove(parent);
        } else {
            throw new Error("Parent node does not exist");
        }
    }
    
    public Set<Node> getParents() {
        return this.parentNodes;
    }
    
    public void clearParents() {
        this.parentNodes = new HashSet<Node>();
    }
    
    public Node[] getChildren() {
        return childNodes;
    }
    
    public boolean isTrueBranch(Node node) {
        throw new Error("Undefined method");
    }
    
    public void setTrueBranch(Node node) {
        if (this.isFork()) {
            childNodes[0] = node;
            node.addParentNode(this);
        } else {
            throw new Error("Unsupported method for non fork nodes");
        }
    }
    
    public void setFalseBranch(Node node) {
        if (this.isFork()) {
            childNodes[1] = node;
            node.addParentNode(this);
        } else {
            throw new Error("Unsupported method for non fork nodes");
        }
    }
    
    public void setNextBranch(Node node) {
        if (!this.isFork()) {
            childNodes[0] = node;
            node.addParentNode(this);
        } else {
            throw new Error("Unsupported method for fork nodes");
        }
    }
    
    public Node getTrueBranch() {
        if (this.isFork()) {
            return childNodes[0];
        } else {
            throw new Error("Unsupported method for non fork nodes");
        }
    }
    
    public Node getFalseBranch() {
        if (this.isFork()) {
            return childNodes[1];
        } else {
            throw new Error("Unsupported method for non fork nodes");
        }
    }
    
    public Node getNextBranch() {
        if (!this.isFork()) {
            return childNodes[0];
        } else {
            throw new Error("Unsupported method for fork nodes");
        }
    }
    
    public boolean isFork() {
        return false;
    }
    
    public boolean isMerge() {
        return parentNodes.size() > 1;
    }
    
    public boolean isNoOp() {
        return false;
    }
    
    public boolean hasNext() {
        return true;
    }
    
    //public abstract String nodeString();
    
    public boolean hasParentBlock() {
        return parentBlock != null;
    }
    
    public CfgBlock getParentBlock() {
        if (this.hasParentBlock()) {
            return (CfgBlock)parentBlock;
        } else {
            throw new Error("Node does not have parent block");
        }
    }
    
    public void setParentBlock(Node block) {
        parentBlock = block;
    }
    
    public void lock() {};
    
    public void unlock() {};
    
    public abstract <T> T accept(NodeVisitor<T> v);
    
    public void visit() {
        visited = true;
    }
    
    public boolean isVisited() {
        return visited;
    }
    
    public void prepend(Node newPredecessor) {
                
        // Reconnect parent nodes
        for (Node parent : this.getParents()) {
            Node[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(this)) {
                    if (parent.isFork()) {
                        if (parent.isTrueBranch(this)) {
                            parent.setTrueBranch(newPredecessor);
                        } else {
                            parent.setFalseBranch(newPredecessor);
                        }
                    } else {
                        parent.setNextBranch(newPredecessor);
                    }
                    newPredecessor.addParentNode(parent);
                }
            }
        }
        
        // Reconnect to new predecessor
        this.clearParents();
        this.addParentNode(newPredecessor);
        newPredecessor.setNextBranch(this);
    }
    
    public IrExpression getExp() {
        throw new UnsupportedOperationException();
    }
    
    public void setExp(IrExpression exp) {
        throw new UnsupportedOperationException();
    }
    
    public Set<IrIdentifier> getLiveVars() {
        return this.liveVars;
    }
    
    public void setLiveVar(Set<IrIdentifier> vars) {
        this.liveVars = new HashSet<IrIdentifier>(vars);
    }
    
    public void delete() {
        // Link parents to child
        for (Node parent : this.parentNodes) {
            if (parent.isFork()) {
                if (parent.isTrueBranch(this)) {
                    parent.setTrueBranch(this.getNextBranch());
                } else {
                    parent.setFalseBranch(this.getNextBranch());
                }
            } else {
                parent.setNextBranch(this.getNextBranch());
            }
        }
        
        // Link children to parent
        Node children = this.getNextBranch();
        children.parentNodes.remove(this);
        children.parentNodes.addAll(this.getParents());
    }
    
    public boolean isStatement() {
        return false;
    }
        
}
