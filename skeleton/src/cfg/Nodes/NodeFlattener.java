package cfg.Nodes;

import java.util.List;

import cfg.MethodCFG;
import ir.DestructIr;
import ir.IrFlattener;
import ir.Declaration.*;
import ir.Statement.*;

/**
 * @author Nicola
 */
public class NodeFlattener implements NodeVisitor<Void> {
    
    private IrFlattener flattener;
    private MethodCFG currentMethod;
    
    public NodeFlattener(MethodCFG currentMethod) {
        this.flattener = new IrFlattener();
        this.currentMethod = currentMethod;
        
    }

    @Override
    public Void visit(CfgBlock node) {
        return null;
    }

    @Override
    public Void visit(CfgCondBranch node) {
            
        // Returns expression --> flatten expression 
        DestructIr flattenedIr = node.getCond().accept(flattener);
        IrBlock destructBlock;
        
        // Check if expression was simplified
        try {
            destructBlock = flattenedIr.getDestructBlock();
        } catch (NoSuchFieldException e) {
            return null;
        }
        
        // Include simplified nodes
        for (IrVariableDeclaration tmpDecl : destructBlock.getVarDecl()) {
            this.currentMethod.addLocal(tmpDecl);
        }
        adjoinStatements(node, destructBlock.getStatements());
        node.setCond(flattenedIr.getSimplifiedExp());
        return null;
    }

    @Override
    public Void visit(CfgEntryNode node) {
        return null;
    }

    @Override
    public Void visit(CfgExitNode node) {
        // No expression returned
        if (!node.returnsExp()) {
            return null;
        }
            
        // Returns expression --> flatten expression 
        DestructIr flattenedIr = node.getReturnExp().accept(flattener);
        IrBlock destructBlock;
        
        // Check if expression was simplified
        try {
            destructBlock = flattenedIr.getDestructBlock();
        } catch (NoSuchFieldException e) {
            return null;
        }
        
        // Include simplified nodes
        for (IrVariableDeclaration tmpDecl : destructBlock.getVarDecl()) {
            this.currentMethod.addLocal(tmpDecl);
        }
        adjoinStatements(node, destructBlock.getStatements());
        node.setReturnExp(flattenedIr.getSimplifiedExp());
        return null;
    }

    @Override
    public Void visit(CfgStatement node) {
        
        // Flatten statements 
        DestructIr flattenedIr = node.getStatement().accept(flattener);
        IrBlock destructBlock;
        
        // Check if expression was simplified
        try {
            destructBlock = flattenedIr.getDestructBlock();
        } catch (NoSuchFieldException e) {
            return null;
        }
        
        // Include simplified nodes
        for (IrVariableDeclaration tmpDecl : destructBlock.getVarDecl()) {
            this.currentMethod.addLocal(tmpDecl);
        }
        adjoinStatements(node, destructBlock.getStatements());
        node.setStatement(flattenedIr.getSimplifiedStm());
        return null;
    }
    
    
    private void adjoinStatements(Node node, List<IrStatement> newStatements) {
        
        CfgNoOp newNode = new CfgNoOp();
        
        // Replace parent link
        for (Node parent : node.getParents()) {
            if (parent.isFork()) {
                if (parent.isTrueBranch(node)) {
                    currentMethod.addTrueEdge(parent, newNode);
                } else {
                    currentMethod.addFalseEdge(parent, newNode);
                }
            } else {
                currentMethod.addNextEdge(parent, newNode);
            }
        }
        
        // Create new branch of nodes
        Node nextNode = newNode;
        for (IrStatement stat : newStatements) {
            currentMethod.addNextEdge(nextNode, new CfgStatement(stat));
            nextNode = nextNode.getNextBranch();
        }  
        
        // Set original node at end of new branch
        node.clearParents();
        currentMethod.addNextEdge(nextNode, node);
    }

}
