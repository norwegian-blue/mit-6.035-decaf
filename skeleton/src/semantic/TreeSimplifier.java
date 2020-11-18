package semantic;

import java.util.List;
import ir.*;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

/**
 * @author Nicola
 */
public class TreeSimplifier implements IrVisitor<Ir> {

    @Override
    public Ir visit(IrClassDeclaration node) {
        
        List<IrFieldDeclaration> fields = node.getFields();
        for (int i = 0; i < fields.size(); i++) {
            fields.set(i, (IrFieldDeclaration) fields.get(i).accept(this));
        }
        
        List<IrMethodDeclaration> methods = node.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            methods.set(i, (IrMethodDeclaration) methods.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrFieldDeclaration node) {
        return node;
    }

    @Override
    public Ir visit(IrMethodDeclaration node) {
        
        List<IrParameterDeclaration> parameters = node.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, (IrParameterDeclaration) parameters.get(i).accept(this));
        }
        
        IrBlock block = node.getBody();
        block = (IrBlock) block.accept(this);
        
        IrMethodDeclaration newNode = new IrMethodDeclaration(node.getId(), node.getType(), parameters, block);
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrParameterDeclaration node) {
        return node;
    }

    @Override
    public Ir visit(IrVariableDeclaration node) {
        return node;
    }

    @Override
    public Ir visit(IrBinaryExpression node) {
        IrBinaryExpression newNode = new IrBinaryExpression(node.getOp(),
                                                (IrExpression) node.getLHS().accept(this), 
                                                (IrExpression) node.getRHS().accept(this));
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrBooleanLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrCalloutExpression node) {
        
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, (IrExpression) args.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrStringLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrIdentifier node) {
        if (node.isArrayElement()) {
            IrIdentifier newNode = new IrIdentifier(node.getId(), (IrExpression) node.getInd().accept(this));
            copyLocation(node, newNode);
            return newNode;
        } else {
            return node;
        }
    }

    @Override
    public Ir visit(IrMethodCallExpression node) {
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, (IrExpression) args.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrUnaryExpression node) {
        Ir outNode;
        IrUnaryExpression simpleNode = new IrUnaryExpression(node.getOp(), (IrExpression) node.getExp().accept(this));
        
        if (simpleNode.getExp() instanceof IrIntLiteral) {
            IrIntLiteral intNode = (IrIntLiteral) simpleNode.getExp();
            try {
                intNode.negate();
                outNode = intNode;
            } catch (NumberFormatException e) {
                // do nothing, will be caught by semantic checker
                outNode = simpleNode;
            }
            
        } else {
            outNode = simpleNode;
        }
        
        copyLocation(node, outNode);
        return outNode;
    }

    @Override
    public Ir visit(IrIntLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrAssignment node) {
        IrAssignment newNode = new IrAssignment((IrIdentifier) node.getLocation().accept(this),
                                                node.getOp(), 
                                                (IrExpression) node.getExpression().accept(this));
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrBlock node) {
        List<IrVariableDeclaration> vars = node.getVarDecl();
        for (int i = 0; i < vars.size(); i++) {
            vars.set(i, (IrVariableDeclaration) vars.get(i).accept(this));
        }
        
        List<IrStatement> statements = node.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            statements.set(i, (IrStatement) statements.get(i).accept(this));
        }
        return node;
    }

    @Override
    public Ir visit(IrBreakStatement node) {
        return node;
    }

    @Override
    public Ir visit(IrContinueStatement node) {
        return node;
    }

    @Override
    public Ir visit(IrForStatement node) {
        IrForStatement newNode = new IrForStatement((IrIdentifier) node.getLoopVar().accept(this),
                                                      (IrExpression) node.getStartExp().accept(this),
                                                      (IrExpression) node.getEndExp().accept(this),
                                                      (IrBlock) node.getLoopBlock().accept(this));
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrIfStatement node) {
        IrIfStatement newNode = new IrIfStatement((IrExpression) node.getCondition().accept(this),
                                                     (IrBlock) node.getThenBlock().accept(this), 
                                                     (IrBlock) node.getElseBlock().accept(this));
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrInvokeStatement node) {
        IrInvokeStatement newNode = new IrInvokeStatement((IrCallExpression) node.getMethod().accept(this));
        copyLocation(node, newNode);
        return newNode;
    }

    @Override
    public Ir visit(IrReturnStatement node) {
        if (node.returnsVoid()) {
            return node;
        } else {
            IrReturnStatement newNode = new IrReturnStatement((IrExpression) node.getReturnExp().accept(this));
            copyLocation(node, newNode);
            return newNode;
        }
    }
    
    private static void copyLocation(Ir nodeFrom, Ir nodeTo) {
        nodeTo.setColNum(nodeFrom.getColNum());
        nodeTo.setLineNum(nodeFrom.getLineNum());
    }

}
