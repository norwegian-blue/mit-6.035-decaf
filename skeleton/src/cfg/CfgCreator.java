package cfg;

import java.util.Stack;

import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

public class CfgCreator implements IrVisitor<CFG> {
    
    private Stack<CFG> loopStart;
    private Stack<CFG> loopEnd;
    
    // Declarations

    @Override
    public CFG visit(IrClassDeclaration node) {
        throw new Error("IrClassDeclaration does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrFieldDeclaration node) {
        throw new Error("IrFieldDeclaration does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrMethodDeclaration node) {
        CFG methodCFG = CFG.makeSingleNode(new CfgEntryNode());        
        methodCFG.concatenate(node.getBody().accept(this));        
        return methodCFG;    
    }

    @Override
    public CFG visit(IrParameterDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrVariableDeclaration node) {
        return CFG.makeSingleNode(new CfgDeclaration(node));
    }

    
    // Expressions
    
    @Override
    public CFG visit(IrBinaryExpression node) {
        throw new Error("IrBinaryExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrBooleanLiteral node) {
        throw new Error("IrBooleanLiteral does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrCalloutExpression node) {
        throw new Error("IrCalloutExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrStringLiteral node) {
        throw new Error("IrStringLiteral does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrIdentifier node) {
        throw new Error("IrIdentifier does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrMethodCallExpression node) {
        throw new Error("IrMethodCallExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrUnaryExpression node) {
        throw new Error("IrUnaryExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrIntLiteral node) {
        throw new Error("IrIntLiteral does not serve CfgCreator");
    }
    
    
    // Statements

    @Override
    public CFG visit(IrAssignment node) {
        return CFG.makeSingleNode(new CfgStatement(node));
    }

    @Override
    public CFG visit(IrBlock node) {
        
        CFG block = CFG.makeNoOp();
        
        for (IrVariableDeclaration decl : node.getVarDecl()) {
            block.concatenate(new CfgDeclaration (decl));
        }
        
        for (IrStatement stat : node.getStatements()) {
            block.concatenate(stat.accept(this));
        }
        
        return block;
    }

    @Override
    public CFG visit(IrBreakStatement node) {
        CFG breakNode = CFG.makeNoOp();
        breakNode.concatenate(loopEnd.peek());
        return breakNode;
    }

    @Override
    public CFG visit(IrContinueStatement node) {
        CFG continueNode = CFG.makeNoOp();
        continueNode.concatenate(loopStart.peek());
        return continueNode;
    }

    @Override
    public CFG visit(IrForStatement node) {
        CFG start = CFG.makeNoOp();
        CFG end = CFG.makeNoOp();
        
        loopStart.push(start);
        loopEnd.push(end);
        
        // Declare loop variable
        IrIdentifier loopVar = node.getLoopVar();
        IrVariableDeclaration loopDecl = new IrVariableDeclaration(loopVar.getExpType(), loopVar.getId());
        CFG forLoop = CFG.makeSingleNode(new CfgDeclaration(loopDecl));     
        
        // Initialize loop variable
        IrStatement initVar = new IrAssignment(loopVar, IrAssignment.IrAssignmentOp.ASSIGN, node.getStartExp());
        forLoop.concatenate(new CfgStatement(initVar));
        forLoop.concatenate(start);        
        
        // Add loop block
        forLoop.concatenate(node.getLoopBlock().accept(this));
        
        // Increase loop variable
        IrStatement incVar = new IrAssignment(loopVar, IrAssignment.IrAssignmentOp.INC, new IrIntLiteral("1"));
        forLoop.concatenate(new CfgStatement(incVar));
        
        // Loop condition
        IrExpression loopCond = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.LT, loopVar, node.getEndExp());
        forLoop.concatenate(shortCircuit(loopCond, start, end));
        
        loopStart.pop();
        loopEnd.pop();
        
        return forLoop;
    }

    @Override
    public CFG visit(IrIfStatement node) {
        CFG ifBranch = node.getThenBlock().accept(this);
        CFG elseBranch = node.getElseBlock().accept(this);
        
        return shortCircuit(node.getCondition(), ifBranch, elseBranch);
    }

    @Override
    public CFG visit(IrInvokeStatement node) {
        return CFG.makeSingleNode(new CfgStatement(node));
    }

    @Override
    public CFG visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    // Private

    private CFG shortCircuit(IrExpression cond, CFG trueBranch, CFG falseBranch) {
        
        CFG graph = CFG.makeNoOp();
        
        if (cond.isAndExp()) {
            IrExpression c1 = ((IrBinaryExpression)cond).getLHS();
            IrExpression c2 = ((IrBinaryExpression)cond).getRHS();
            
            CFG b2 = shortCircuit(c2, trueBranch, falseBranch);
            graph = shortCircuit(c1, b2, falseBranch);
            
        } else if (cond.isOrExp()) {
            IrExpression c1 = ((IrBinaryExpression)cond).getLHS();
            IrExpression c2 = ((IrBinaryExpression)cond).getRHS();
            
            CFG b2 = shortCircuit(c2, trueBranch, falseBranch);
            graph = shortCircuit(c1, b2, trueBranch);
            
        } else if (cond.isNotExp()) {
            IrExpression c = ((IrUnaryExpression)cond).getExp();
            graph = shortCircuit(c, falseBranch, trueBranch);
            
        } else {
            CfgBranch cfgCond = new CfgBranch(cond);
            graph.concatenate(cfgCond);
            graph.addBranches(trueBranch, falseBranch);
        }
        
        return graph;
    }
    
}
