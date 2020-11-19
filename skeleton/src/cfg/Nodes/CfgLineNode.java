package cfg.Nodes;

public abstract class CfgLineNode extends Node {
    
    public CfgLineNode() {
        this.childNodes = new Node[1];
    }
    
    @Override
    public void setNextBranch(Node next) {
        next.addParentNode(this);
        this.childNodes[0] = next;
    }
    
    @Override
    public String toString() {
        String nodeStr = this.nodeString();
        if (this.hasNext()) {
            nodeStr += " --> " + this.getNextBranch().nodeString();
        }
        return nodeStr;
    }
    
    public boolean hasNext() {
        return true;
    }
}
