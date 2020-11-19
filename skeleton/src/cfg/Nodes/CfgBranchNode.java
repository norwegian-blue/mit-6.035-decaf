package cfg.Nodes;

/**
 * @author Nicola
 */

public class CfgBranchNode extends Node {
    
    public CfgBranchNode() {
        this.childNodes = new Node[2];
    }
    
    @Override
    public boolean isFork() {
        return true;
    }
    
    @Override
    public void setTrueBranch(Node node) {
        this.childNodes[0] = node;
    }
    
    @Override
    public void setFalseBranch(Node node) {
        this.childNodes[1] = node;
    }
    
    @Override
    public boolean isTrueBranch(Node node) {
        return this.getTrueBranch().equals(node);
    }

}
