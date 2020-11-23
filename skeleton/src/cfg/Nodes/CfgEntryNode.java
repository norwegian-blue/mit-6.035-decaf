package cfg.Nodes;

import java.util.Set;

/**
 * @author Nicola
 */

public class CfgEntryNode extends CfgLineNode {
    
    public CfgEntryNode() {
        super();
    }
        
    @Override
    public Set<Node> getParents() {
        throw new Error("Entry node does not have parent nodes");
    }
    
    @Override
    public String nodeString() {
        return "NODE ENTRY";
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return v.visit(this);
    }
}