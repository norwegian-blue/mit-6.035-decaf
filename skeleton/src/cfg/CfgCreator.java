package cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cfg.Nodes.*;
import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

public class CfgCreator implements IrVisitor<DestructNodes> {
    
    private Stack<Node> loopStart = new Stack<Node>();
    private Stack<Node> loopEnd = new Stack<Node>();
    private static List<IrVariableDeclaration> locals = new ArrayList<IrVariableDeclaration>();
    
    public static MethodCFG BuildMethodCFG(IrMethodDeclaration method) {
        CfgCreator creator = new CfgCreator();
        Node root = method.accept(creator).getBeginNode();
        MethodCFG CFG = new MethodCFG(root);
        for (IrVariableDeclaration local : locals) {
            CFG.addLocal(local);
        }
        System.out.println(CFG);
        CFG.removeNoOps();
        System.out.println(CFG);
        return CFG;
    }
    
    // Declarations

    @Override
    public DestructNodes visit(IrClassDeclaration node) {
        throw new Error("IrClassDeclaration does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrFieldDeclaration node) {
        throw new Error("IrFieldDeclaration does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrMethodDeclaration node) {
        DestructNodes methodBlock = new DestructNodes(new CfgEntryNode());
        methodBlock.concatenate(node.getBody().accept(this));
        return methodBlock;
    }

    @Override
    public DestructNodes visit(IrParameterDeclaration node) {
        throw new Error("IrVariabDeclaration does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrVariableDeclaration node) {
        throw new Error("IrVariabDeclaration does not serve CfgCreator");
    }

    
    // Expressions
    
    @Override
    public DestructNodes visit(IrBinaryExpression node) {
        throw new Error("IrBinaryExpression does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrBooleanLiteral node) {
        throw new Error("IrBooleanLiteral does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrCalloutExpression node) {
        throw new Error("IrCalloutExpression does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrStringLiteral node) {
        throw new Error("IrStringLiteral does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrIdentifier node) {
        throw new Error("IrIdentifier does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrMethodCallExpression node) {
        throw new Error("IrMethodCallExpression does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrUnaryExpression node) {
        throw new Error("IrUnaryExpression does not serve CfgCreator");
    }

    @Override
    public DestructNodes visit(IrIntLiteral node) {
        throw new Error("IrIntLiteral does not serve CfgCreator");
    }
    
    
    // Statements

    @Override
    public DestructNodes visit(IrAssignment node) {
        return new DestructNodes(new CfgStatement(node));
    }

    @Override
    public DestructNodes visit(IrBlock node) {
               
        for (IrVariableDeclaration decl : node.getVarDecl()) {
            locals.add(decl);
        }
        
        DestructNodes block = new DestructNodes(new CfgNoOp());
        for (IrStatement stat : node.getStatements()) {
            block.concatenate(stat.accept(this));
        }
        
        return block;
    }

    @Override
    public DestructNodes visit(IrBreakStatement node) {
        // TODO break statement
        return null;
    }

    @Override
    public DestructNodes visit(IrContinueStatement node) {
        // TODO continue statement
        return null;
    }

    @Override
    public DestructNodes visit(IrForStatement node) {
        // TODO for statement
        return null;
//        CFG start = CFG.makeNoOp();
//        CFG end = CFG.makeNoOp();
//        
//        loopStart.push(start);
//        loopEnd.push(end);
//        
//        // Declare loop variable
//        IrIdentifier loopVar = node.getLoopVar();
//        IrVariableDeclaration loopDecl = new IrVariableDeclaration(BaseTypeDescriptor.INT, loopVar.getId());
//        CFG forLoop = CFG.makeSingleNode(new CfgDeclaration(loopDecl));     
//        
//        // Initialize loop variable
//        IrStatement initVar = new IrAssignment(loopVar, IrAssignment.IrAssignmentOp.ASSIGN, node.getStartExp());
//        forLoop.concatenate(new CfgStatement(initVar));
//        forLoop.concatenate(start);        
//        
//        // Add loop block
//        forLoop.concatenate(node.getLoopBlock().accept(this));
//        
//        // Increase loop variable
//        IrStatement incVar = new IrAssignment(loopVar, IrAssignment.IrAssignmentOp.INC, new IrIntLiteral("1"));
//        forLoop.concatenate(new CfgStatement(incVar));
//        
//        // Loop condition
//        IrExpression loopCond = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.LT, loopVar, node.getEndExp());
//        forLoop.concatenate(shortCircuit(loopCond, start, end));
//        
//        loopStart.pop();
//        loopEnd.pop();
//        
//        return forLoop;
    }

    @Override
    public DestructNodes visit(IrIfStatement node) {
        DestructNodes ifBranch = node.getThenBlock().accept(this);
        DestructNodes elseBranch = node.getElseBlock().accept(this);
        
        Node cond = shortCircuit(node.getCondition(), ifBranch, elseBranch);
        Node merge = new CfgNoOp();
        
        ifBranch.getEndNode().setNextBranch(merge);
        elseBranch.getBeginNode().setNextBranch(merge);
        
        return new DestructNodes(cond, merge);
    }

    @Override
    public DestructNodes visit(IrInvokeStatement node) {
        return new DestructNodes(new CfgStatement(node));
    }

    @Override
    public DestructNodes visit(IrReturnStatement node) {
        // TODO return expression
        return new DestructNodes(new CfgExitNode());
    }
    
    
    // Private

    private CfgCondBranch shortCircuit(IrExpression cond, DestructNodes trueBranch, DestructNodes falseBranch) {
        
        CfgCondBranch branch;
        
        if (cond.isAndExp()) {
            IrExpression c1 = ((IrBinaryExpression)cond).getLHS();
            IrExpression c2 = ((IrBinaryExpression)cond).getRHS();
            
            CfgCondBranch b2 = shortCircuit(c2, trueBranch, falseBranch);
            DestructNodes B2 = new DestructNodes(b2);
            branch = shortCircuit(c1, B2, falseBranch);
            
        } else if (cond.isOrExp()) {
            IrExpression c1 = ((IrBinaryExpression)cond).getLHS();
            IrExpression c2 = ((IrBinaryExpression)cond).getRHS();
            
            CfgCondBranch b2 = shortCircuit(c2, trueBranch, falseBranch);
            DestructNodes B2 = new DestructNodes(b2);
            branch = shortCircuit(c1, B2, trueBranch);
            
        } else if (cond.isNotExp()) {
            IrExpression c = ((IrUnaryExpression)cond).getExp();
            branch = shortCircuit(c, falseBranch, trueBranch);
            
        } else {
            branch = new CfgCondBranch(cond);
            branch.setTrueBranch(trueBranch.getBeginNode());
            branch.setFalseBranch(falseBranch.getBeginNode());
        }
        
        return branch;
    }
    
}

class DestructNodes {
    
    private Node begin;
    private Node end;
    
    public DestructNodes(Node begin, Node end) {
        this.begin = begin;
        this.end = end;
    }
    
    public DestructNodes(Node node) {
        this.begin = node;
        this.end = node;
    }
    
    public void setBeginNode(Node node) {
        this.begin = node;
    }
    
    public void setEndNode(Node node) {
        this.end = node;
    }
    
    public Node getBeginNode() {
        return this.begin;
    }
    
    public Node getEndNode() {
        return this.end;
    }
    
    public void concatenate(DestructNodes next) {
        this.end.setNextBranch(next.getBeginNode());
        this.setEndNode(next.getEndNode());
    }
}
