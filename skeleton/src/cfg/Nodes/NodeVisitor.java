package cfg.Nodes;

/**
 * @author Nicola
 */

public interface NodeVisitor<T> {
    
    public T visit(CfgBlock node);
    
    public T visit(CfgCondBranch node);
    
    public T visit(CfgEntryNode node);
    
    public T visit(CfgExitNode node);

    public T visit(CfgStatement node);   
    
}
