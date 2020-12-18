package cfg.Optimization;

import java.util.HashMap;
import java.util.Map;

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
public class GenExpressionFinder implements NodeVisitor<Void>{
    
    private Map<IrExpression, Node> generatedExpressions;
    
    public GenExpressionFinder() {
        this.generatedExpressions = new HashMap<IrExpression, Node>();
    }
    
    public Map<IrExpression, Node> getGenExpression() {
        return this.generatedExpressions;
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
        IrExpression exp = node.getCond();
        switch (exp.getExpKind()) {
        case BOOL:
        case CALL:
        case ID:
        case INT:
        case METH:
        case STRING:
            return null;
        default:
            break;
        }
        
        this.generatedExpressions.put(exp, node);
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
        
        // Skip non binary/unary expressions
        IrAssignment ass = (IrAssignment) node.getStatement();
        switch (ass.getExpression().getExpKind()) {
        case BOOL:
        case CALL:
        case ID:
        case INT:
        case METH:
        case STRING:
            return null;
        default:
            break;
        }
        
        IrExpression exp = ass.getExpression();
        
        // Add expression
        this.generatedExpressions.put(exp, node);
        
        // Remove expression w/ reassigned term
        this.clear(ass.getLocation());
        
        return null;
    }
    
    private void clear(IrIdentifier id) {
        for (IrExpression exp : this.generatedExpressions.keySet()) {
            if (exp.contains(id)) {
                this.generatedExpressions.remove(exp);
            }
        }
    }

}
