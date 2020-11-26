package cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cfg.Nodes.*;
import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import semantic.*;

public class CFGCreator implements IrVisitor<DestructNodes> {
    
    private Stack<Node> loopContinue = new Stack<Node>();
    private Stack<Node> loopEnd = new Stack<Node>();
    private static List<IrVariableDeclaration> locals = new ArrayList<IrVariableDeclaration>();
    
    public static MethodCFG BuildMethodCFG(IrMethodDeclaration method) {
        CFGCreator creator = new CFGCreator();
        Node root = method.accept(creator).getBeginNode(); 
        
        // Get method parameters
        List<ParameterDescriptor> parDesc = new ArrayList<ParameterDescriptor>();
        for (IrParameterDeclaration par : method.getParameters()) {
            parDesc.add(new ParameterDescriptor(par.getId(), par.getType()));
        }
        
        // Get locals
        List<LocalDescriptor> localDesc = new ArrayList<LocalDescriptor>();
        for (IrVariableDeclaration local : locals) {
            localDesc.add(new LocalDescriptor(local.getId(), local.getType()));
        }
        
        // Get method descriptor
        MethodDescriptor methodDesc = new MethodDescriptor(method.getId(), method.getType(), parDesc, localDesc);
        MethodCFG CFG = new MethodCFG(root, methodDesc);

        //System.out.println("############## RAW CFG ##############\n" + CFG + "\n");
        CFG.removeNoOps();
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
        
        // Terminate if no return was found code
        if (methodBlock.getEndNode().hasNext()) {
            methodBlock.concatenate(new DestructNodes(new CfgExitNode()));
        }
        
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
        CfgNoOpLock breakNode = new CfgNoOpLock();
        breakNode.setNextBranch(loopEnd.peek());
        breakNode.lock();
        DestructNodes breakBlock = new DestructNodes(breakNode);
        return breakBlock;
    }

    @Override
    public DestructNodes visit(IrContinueStatement node) {
        CfgNoOpLock continueNode = new CfgNoOpLock();
        continueNode.setNextBranch(loopContinue.peek());
        continueNode.lock();
        DestructNodes continueBlock = new DestructNodes(continueNode);
        return continueBlock;
    }

    @Override
    public DestructNodes visit(IrForStatement node) {
        
        // Loop variable declaration
        IrIdentifier forVar = node.getLoopVar();
        IrVariableDeclaration forDecl = new IrVariableDeclaration(BaseTypeDescriptor.INT, forVar.getId());
        locals.add(forDecl);
        
        // Loop variable initialization
        IrAssignment forInit = new IrAssignment(forVar, IrAssignment.IrAssignmentOp.ASSIGN, node.getStartExp());
        Node forInitNode = new CfgStatement(forInit);
        DestructNodes forLoop = new DestructNodes(forInitNode);
        
        // Increment loop variable
        IrAssignment loopInc = new IrAssignment(forVar, IrAssignment.IrAssignmentOp.INC, new IrIntLiteral("1"));
        Node loopContinueNode = new CfgStatement(loopInc);
        DestructNodes incBlock = new DestructNodes(loopContinueNode);
        
        // Loop block
        Node forStartNode = new CfgNoOp();
        Node forEndNode = new CfgNoOp();
        loopContinue.push(loopContinueNode);
        loopEnd.push(forEndNode);
        DestructNodes forBlock = node.getLoopBlock().accept(this);       
        forBlock.concatenate(incBlock);
        
        // Loop
        IrExpression forCond = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.LT, forVar, node.getEndExp());
        forBlock.getEndNode().setNextBranch(forStartNode);
        DestructNodes forEnd = new DestructNodes(forEndNode);
        forStartNode.setNextBranch(shortCircuit(forCond, forBlock, forEnd));
        forLoop.getEndNode().setNextBranch(forStartNode);
        forLoop.setEndNode(forEndNode);
               
        // Clean up
        loopContinue.pop();
        loopEnd.pop();
        return forLoop;

    }

    @Override
    public DestructNodes visit(IrIfStatement node) {
        DestructNodes ifBranch = node.getThenBlock().accept(this);
        DestructNodes elseBranch = node.getElseBlock().accept(this);
        
        Node cond = shortCircuit(node.getCondition(), ifBranch, elseBranch);
        Node merge = new CfgNoOp();
        
        ifBranch.getEndNode().setNextBranch(merge);
        elseBranch.getEndNode().setNextBranch(merge);
        
        return new DestructNodes(cond, merge);
    }

    @Override
    public DestructNodes visit(IrInvokeStatement node) {
        return new DestructNodes(new CfgStatement(node));
    }

    @Override
    public DestructNodes visit(IrReturnStatement node) {
        CfgExitNode exitNode;
        if (node.returnsValue()) {
            exitNode = new CfgExitNode(node.getReturnExp());
        } else {
            exitNode = new CfgExitNode();
        }
        return new DestructNodes(exitNode);
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
