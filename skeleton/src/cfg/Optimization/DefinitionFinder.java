package cfg.Optimization;

import java.util.HashSet;
import java.util.Set;

import cfg.Nodes.CfgBlock;
import cfg.Nodes.CfgCondBranch;
import cfg.Nodes.CfgEntryNode;
import cfg.Nodes.CfgExitNode;
import cfg.Nodes.CfgStatement;
import cfg.Nodes.Node;
import cfg.Nodes.NodeVisitor;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;

/** 
* @author Nicola
*/
public class DefinitionFinder implements NodeVisitor<Void>{
    
    private Set<IrIdentifier> definitions;
    
    public DefinitionFinder() {
        this.definitions = new HashSet<IrIdentifier>();
    }
    
    public Set<IrIdentifier> getDefinitions() {
        return this.definitions;
    }

    @Override
    public Void visit(CfgBlock node) {
        for (Node subNode : node.getBlockNodes()) {
            subNode.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CfgCondBranch node) {
        return null;
    }

    @Override
    public Void visit(CfgEntryNode node) {
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        return null;
    }

    @Override
    public Void visit(CfgStatement node) {
        // Skip non-assignemnt
        if (!node.getStatement().isAssignment()) {
            return null;
        }
              
        // Add definition
        IrAssignment ass = (IrAssignment) node.getStatement();
        this.definitions.add(ass.getLocation());
        
        return null;
    }

}
