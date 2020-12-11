package ir.Statement;

import ir.Ir;
import ir.IrVisitor;
import ir.Expression.*;

/**
 * @author Nicola
 */
public class IrAssignment extends IrStatement {
    private final IrIdentifier location;
    private final IrAssignmentOp assignOp;
    private IrExpression expr;
    
    public enum IrAssignmentOp {
        ASSIGN,
        INC,
        DEC
    }
    
    public IrAssignment(IrIdentifier location, IrAssignmentOp assignOp, IrExpression expr) {
        this.location = location;
        this.assignOp = assignOp;
        this.expr = expr;
    }
    
    public IrIdentifier getLocation() {
        return this.location;
    }
    
    public IrAssignmentOp getOp() {
        return this.assignOp;
    }
    
    public IrExpression getExpression() {
        return this.expr;
    }
    
    public void setExpression(IrExpression expr) {
        this.expr = expr;
    }
    
    @Override
    public String toString() {
        if (!printAsTree) {
            return inLineStr();
        }
        return assignOp.toString() + "\n" + Ir.indent(location.toString()) + Ir.indent("\n<--\n") + Ir.indent(expr.toString());
    }
    
    private String inLineStr() {
        return location.toString() + " " + assignOp.toString() + " " + expr.toString();
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
}
