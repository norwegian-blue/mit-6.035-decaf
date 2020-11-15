package cfg.Nodes;

/**
 * @author Nicola
 */

public class CfgEntryNode extends CfgNode {

    public CfgEntryNode() {
        super();
        this.addParentNode(null);
    }
    
    @Override
    public String toString() {
        return "ENTRY";
    }
    
}