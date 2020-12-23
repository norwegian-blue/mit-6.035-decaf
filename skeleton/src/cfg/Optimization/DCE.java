package cfg.Optimization;


import java.util.Iterator;

import cfg.MethodCFG;
import cfg.Nodes.CfgBlock;
import cfg.Nodes.CfgCondBranch;
import cfg.Nodes.CfgEntryNode;
import cfg.Nodes.CfgExitNode;
import cfg.Nodes.CfgStatement;
import cfg.Nodes.Node;
import cfg.Nodes.NodeVisitor;
import ir.Expression.IrExpression;
import ir.Expression.IrIdentifier;
import ir.Statement.IrAssignment;

/**
 * @author Nicola
 */
public class DCE implements NodeVisitor<Boolean> {
    
    public boolean optimize(MethodCFG cfg){
        
        boolean check = false;
        
        LivenessAnalysis la = new LivenessAnalysis();
        la.analyze(cfg);
        
        for (Node block : cfg.getNodes()) {
            check |= block.accept(this);
        }
        
        return check;
    }

    @Override
    public Boolean visit(CfgBlock node) {
        boolean check = false;
        Iterator<Node> it = node.getBlockNodes().iterator();
        while (it.hasNext()) {
            Node blockNode = it.next();
            if (blockNode.accept(this)) {
                check = true;
                it.remove();
            }
        }
        return check;
    }

    @Override
    public Boolean visit(CfgCondBranch node) {
        return false;
    }

    @Override
    public Boolean visit(CfgEntryNode node) {
        return false;
    }

    @Override
    public Boolean visit(CfgExitNode node) {
        return false;
    }

    @Override
    public Boolean visit(CfgStatement node) {
        if (!node.getStatement().isAssignment()) {
            return false;
        }
        
        boolean check = false;
        
        IrAssignment ass = (IrAssignment) node.getStatement();
        IrIdentifier id = (IrIdentifier) ass.getLocation();
        IrExpression val = (IrExpression) ass.getExpression();
        
        // Remove statement x = x   
        if (val.equals(id)) {
            check = true;
        }
        
        // Remove assignment to dead variable
        if (!node.getLiveVars().contains(id)) {
            check = true;
        }
        
        return check;
    }
    
    

}
