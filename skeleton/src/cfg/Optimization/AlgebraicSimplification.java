package cfg.Optimization;

import ir.Expression.*;

/**
 * @author Nicola
 */

public class AlgebraicSimplification extends ExpressionSimplifier {
            
    @Override
    public IrExpression visit(IrBinaryExpression node) {
              
        IrExpression lhs = node.getLHS().accept(this);
        IrExpression rhs = node.getRHS().accept(this);
        
        switch (node.getOp()) {
        case AND:
            if (lhs.isLiteral() && ((IrBooleanLiteral)lhs).eval() == false) {
                return lhs;
            } else if (rhs.isLiteral() && ((IrBooleanLiteral)rhs).eval() == false)  {
                return rhs;
            } else if (lhs.isLiteral() && ((IrBooleanLiteral)lhs).eval() == true)  {
                return rhs;
            } else if (rhs.isLiteral() && ((IrBooleanLiteral)rhs).eval() == true)  {
                return lhs;
            } else if (lhs.equals(rhs)) {
                return lhs;
            }
            break;
        case DIVIDE:
            if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 0) {
                return lhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 1) {
                return lhs;
            }
            break;
        case EQ:
            break;
        case GE:
            break;
        case GT:
            break;
        case LE:
            break;
        case LT:
            break;
        case MINUS:
            if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 0) {
                rhs = new IrUnaryExpression(IrUnaryExpression.UnaryOperator.MINUS, rhs);
                rhs.setExpType(node.getExpType());
                return rhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 0) {
                return lhs;
            } else if (rhs.equals(lhs)) {
                return new IrIntLiteral("0");
            }
            break;
        case MOD:
            if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 0) {
                return lhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 1) {
                return lhs;
            }
            break;
        case NEQ:
            break;
        case OR:
            if (lhs.isLiteral() && ((IrBooleanLiteral)lhs).eval() == false) {
                return rhs;
            } else if (rhs.isLiteral() && ((IrBooleanLiteral)rhs).eval() == false)  {
                return lhs;
            } else if (lhs.isLiteral() && ((IrBooleanLiteral)lhs).eval() == true)  {
                return lhs;
            } else if (rhs.isLiteral() && ((IrBooleanLiteral)rhs).eval() == true)  {
                return rhs;
            } else if (lhs.equals(rhs)) {
                return lhs;
            }
            break;
        case PLUS:
            if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 0) {
                return rhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 0) {
                return lhs;
            } else if (rhs.isUnaryMinus()) {
                IrExpression exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.MINUS, lhs,
                                                          ((IrUnaryExpression)rhs).getExp());
                exp.setExpType(node.getExpType());
                return exp;
            }    
            break;
        case TIMES:
            if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 0) {
                return lhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 0) {
                return rhs;
            } else if (lhs.isLiteral() && ((IrIntLiteral)lhs).eval() == 1) {
                return rhs;
            } else if (rhs.isLiteral() && ((IrIntLiteral)rhs).eval() == 1) {
                return lhs;
            }
            break;
        default:
            break;
        }
        
        return node;
        
    }
    
    @Override
    public IrExpression visit(IrUnaryExpression node) {
        
        IrExpression exp = node.getExp().accept(this);
        exp.setExpType(node.getExpType());
        
        if (!exp.isNotExp() && !exp.isUnaryMinus()) {
            return node;
        }
        
        IrUnaryExpression unExp = (IrUnaryExpression) exp;
        
        if (unExp.getExp().isNotExp() && unExp.isNotExp()) {
            return ((IrUnaryExpression)unExp.getExp()).getExp();
        } else if (unExp.getExp().isUnaryMinus() && unExp.isUnaryMinus()) {
            return ((IrUnaryExpression)unExp.getExp()).getExp();
        }
        return node;
    }

}
