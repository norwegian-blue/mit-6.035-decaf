package cfg.Optimization;

import java.util.HashMap;
import java.util.Iterator;
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
        this.generatedExpressions = new HashMap<IrExpression, Node>();
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
            if (!node.getStatement().isInvokeStatement()) {
                this.clearAll();
            }    
            return null;
        }
        
        // Skip non binary/unary expressions
        IrAssignment ass = (IrAssignment) node.getStatement();
        switch (ass.getExpression().getExpKind()) {
        case METH:
            this.clearAll();
            break;
        case BIN:
        case UN:
            IrExpression exp = ass.getExpression();
            this.generatedExpressions.put(exp, node);
            break;
        case BOOL:
        case CALL:
        case ID:
        case INT:
        case STRING:
        default:
            break;
        }
        
        // Remove expression w/ reassigned term
        this.clear(ass.getLocation());
        
        return null;
    }
    
    private void clear(IrIdentifier id) {
        
        Iterator<IrExpression> it = this.generatedExpressions.keySet().iterator();
        while (it.hasNext()) {
            IrExpression exp = it.next();
            if (exp.contains(id)) {
                it.remove();
            }
        }
    }
    
    private void clearAll() {
        this.generatedExpressions = new HashMap<IrExpression, Node>();
    }

}
