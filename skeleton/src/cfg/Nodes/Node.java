package cfg.Nodes;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nicola
 */

public abstract class Node {
    
    protected Set<Node> parentNodes = new HashSet<Node>();
    protected Node childNodes[];
        
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
    
    public Node[] getChildren() {
        return childNodes;
    }
    
    public boolean isTrueBranch(Node node) {
        throw new Error("Undefined method");
    }
    
    public void setTrueBranch(Node node) {
        throw new Error("Undefined method");
    }
    
    public void setFalseBranch(Node node) {
        throw new Error("Undefined method");
    }
    
    public void setNextBranch(Node node) {
        throw new Error("Undefined method");
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
    
}
