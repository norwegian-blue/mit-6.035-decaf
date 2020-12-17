package cfg.Optimization;

import ir.Expression.*;
import semantic.BaseTypeDescriptor;

/**
 * @author Nicola
 */

public class ConstantExpressionEvaluation extends ExpressionSimplifier {

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
    public IrExpression visit(IrUnaryExpression node) {
        IrExpression rhs = node.getExp().accept(this);
        if (node.getOp() == IrUnaryExpression.UnaryOperator.NOT && rhs.isLiteral()) {
            boolean boolVar = !((IrBooleanLiteral)rhs).eval();
            return new IrBooleanLiteral(String.valueOf(boolVar));
        }
        return node;
    }
    
}