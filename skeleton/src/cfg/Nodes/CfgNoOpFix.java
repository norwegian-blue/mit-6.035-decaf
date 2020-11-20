package cfg.Nodes;

/**
 * @author Nicola
 */
public class CfgNoOpFix extends CfgNoOp {
    
    @Override
    public void setNextBranch(Node node) {
    }
    
    public void fixNextBranch(Node node) {
        childNodes[0] = node;
        node.addParentNode(this);
    }
    
}
