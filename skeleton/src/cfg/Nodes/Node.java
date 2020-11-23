package cfg.Nodes;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nicola
 */

public abstract class Node {
    
    protected static boolean printIr = false;
    
    public static void setPrintIr(boolean print) {
        printIr = print;
    }
    
    protected Set<Node> parentNodes = new HashSet<Node>();
    protected Node childNodes[];
    private Node parentBlock = null;
        
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
    
    public abstract String nodeString();
    
    public boolean hasParentBlock() {
        return parentBlock != null;
    }
    
    public Node getParentBlock() {
        if (this.hasParentBlock()) {
            return parentBlock;
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
    
}
