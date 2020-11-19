package cfg.Nodes;

public class CfgLineNode extends Node {
    
    public CfgLineNode() {
        this.childNodes = new Node[1];
    }
    
    @Override
    public void setNextBranch(Node next) {
        next.addParentNode(this);
        this.childNodes[0] = next;
    }
}
