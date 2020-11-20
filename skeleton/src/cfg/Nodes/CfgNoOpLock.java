package cfg.Nodes;

/**
 * @author Nicola
 */
public class CfgNoOpLock extends CfgNoOp {
    
    private boolean lock = false;
    
    @Override
    public void setNextBranch(Node node) {
        if (!lock) {
            childNodes[0] = node;
            node.addParentNode(this);
        }
    }
        
    @Override
    public void unlock() {
        this.lock = false;
    }
    
    @Override 
    public void lock() {
        this.lock = true;
    }
    
}
