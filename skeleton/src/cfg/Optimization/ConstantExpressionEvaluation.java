package cfg.Optimization;

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
        
        return node;
    }

    @Override
    public IrExpression visit(IrBooleanLiteral node) {
        return node;
    }

    @Override
    public IrExpression visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IrExpression visit(IrUnaryExpression node) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IrExpression visit(IrIfStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IrExpression visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IrExpression visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    
}