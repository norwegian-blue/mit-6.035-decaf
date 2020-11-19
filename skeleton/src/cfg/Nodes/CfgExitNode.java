package cfg.Nodes;

/**
 * @author Nicola
 */
public class CfgExitNode extends CfgLineNode {
    
    // TODO return expression
       
    public CfgExitNode() {
        super();
    }
    
    @Override
    public String nodeString() {
        return "NODE EXIT";
    }
    
    @Override
    public boolean hasNext() {
        return false;
    }
    
}
