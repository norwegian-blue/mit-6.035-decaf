package cfg.Optimization;

import java.util.List;

import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */

public class ConstantExpressionEvaluation implements IrVisitor<IrExpression>{

    @Override
    public IrExpression visit(IrClassDeclaration node) {
        for (IrMethodDeclaration method : node.getMethods()) {
            method.accept(this);
        }
        return null;
    }

    @Override
    public IrExpression visit(IrFieldDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public IrExpression visit(IrMethodDeclaration node) {
        node.getBody().accept(this);
        return null;
    }

    @Override
    public IrExpression visit(IrParameterDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public IrExpression visit(IrVariableDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public IrExpression visit(IrBinaryExpression node) {
        IrExpression lhs = node.getLHS().accept(this);
        IrExpression rhs = node.getRHS().accept(this);
        
        // Evaluate expression
        if (lhs.isLiteral() && rhs.isLiteral()) {
            int intVal;
            boolean boolVal;
            switch (node.getOp()) {
            case AND:
                boolVal = ((IrBooleanLiteral)lhs).eval() && ((IrBooleanLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case DIVIDE:
                intVal = ((IrIntLiteral)lhs).eval() / ((IrIntLiteral)rhs).eval();
                return new IrIntLiteral(String.valueOf(intVal));
            case EQ:
                if (lhs.getExpType().equals(BaseTypeDescriptor.BOOL)) {
                    boolVal = ((IrBooleanLiteral)lhs).eval() == ((IrBooleanLiteral)rhs).eval();
                } else {
                    boolVal = ((IrIntLiteral)lhs).eval() == ((IrIntLiteral)rhs).eval();
                }
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case GE:
                boolVal = ((IrIntLiteral)lhs).eval() >= ((IrIntLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case GT:
                boolVal = ((IrIntLiteral)lhs).eval() > ((IrIntLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case LE:
                boolVal = ((IrIntLiteral)lhs).eval() <= ((IrIntLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case LT:
                boolVal = ((IrIntLiteral)lhs).eval() < ((IrIntLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case MINUS:
                intVal = ((IrIntLiteral)lhs).eval() - ((IrIntLiteral)rhs).eval();
                return new IrIntLiteral(String.valueOf(intVal));
            case MOD:
                intVal = ((IrIntLiteral)lhs).eval() % ((IrIntLiteral)rhs).eval();
                return new IrIntLiteral(String.valueOf(intVal));
            case NEQ:
                if (lhs.getExpType().equals(BaseTypeDescriptor.BOOL)) {
                    boolVal = ((IrBooleanLiteral)lhs).eval() != ((IrBooleanLiteral)rhs).eval();
                } else {
                    boolVal = ((IrIntLiteral)lhs).eval() != ((IrIntLiteral)rhs).eval();
                }
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case OR:
                boolVal = ((IrBooleanLiteral)lhs).eval() || ((IrBooleanLiteral)rhs).eval();
                return new IrBooleanLiteral(String.valueOf(boolVal));
            case PLUS:
                intVal = ((IrIntLiteral)lhs).eval() + ((IrIntLiteral)rhs).eval();
                return new IrIntLiteral(String.valueOf(intVal));
            case TIMES:
                intVal = ((IrIntLiteral)lhs).eval() * ((IrIntLiteral)rhs).eval();
                return new IrIntLiteral(String.valueOf(intVal));
            default:
                throw new Error("Unexpected operation");
            }
        }
        
        IrExpression newExp = new IrBinaryExpression(node.getOp(), lhs, rhs);
        newExp.setExpType(node.getExpType());
        return newExp;
    }

    @Override
    public IrExpression visit(IrBooleanLiteral node) {
        return node;
    }

    @Override
    public IrExpression visit(IrCalloutExpression node) {
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).accept(this));
        }        
        return node;
    }

    @Override
    public IrExpression visit(IrStringLiteral node) {
        return node;
    }

    @Override
    public IrExpression visit(IrIdentifier node) {
        return node;
    }

    @Override
    public IrExpression visit(IrMethodCallExpression node) {
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, args.get(i).accept(this));
        }        
        return node;
    }

    @Override
    public IrExpression visit(IrUnaryExpression node) {
        IrExpression rhs = node.getExp().accept(this);
        if (node.getOp() == IrUnaryExpression.UnaryOperator.NOT && rhs.isLiteral()) {
            boolean boolVar = !((IrBooleanLiteral)rhs).eval();
            return new IrBooleanLiteral(String.valueOf(boolVar));
        }
        return new IrUnaryExpression(node.getOp(), rhs);
    }

    @Override
    public IrExpression visit(IrIntLiteral node) {
        return node;
    }

    @Override
    public IrExpression visit(IrAssignment node) {
        node.setExpression(node.getExpression().accept(this));
        return null;
    }

    @Override
    public IrExpression visit(IrBlock node) {
        for (IrStatement stat : node.getStatements()) {
            stat.accept(this);
        }
        return null;
    }

    @Override
    public IrExpression visit(IrBreakStatement node) {
        return null;
    }

    @Override
    public IrExpression visit(IrContinueStatement node) {
        return null;
    }

    @Override
    public IrExpression visit(IrForStatement node) {
        node.setStartExp(node.getStartExp().accept(this));
        node.setEndExp(node.getEndExp().accept(this));
        node.getLoopBlock().accept(this);
        return null;
    }

    @Override
    public IrExpression visit(IrIfStatement node) {
        node.setCondition(node.getCondition().accept(this));
        node.getThenBlock().accept(this);
        node.getElseBlock().accept(this);
        return null;
    }

    @Override
    public IrExpression visit(IrInvokeStatement node) {
        node.setMethod((IrCallExpression)node.getMethod().accept(this));
        return null;
    }

    @Override
    public IrExpression visit(IrReturnStatement node) {
        node.setReturnExp(node.getReturnExp().accept(this));
        return null;
    }
    
}