package cfg.Nodes;

public class CfgNoOp extends CfgLineNode {
    
    public CfgNoOp() {
        super();
    }
    
    @Override
    public boolean isNoOp() {
        return true;
    }
    
    @Override
    public String toString() {
        return "NODE NOOP";
    }

    @Override
    public <T> T accept(NodeVisitor<T> v) {
        return null;
    }
    
}
