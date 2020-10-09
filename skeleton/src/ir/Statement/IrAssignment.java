package ir.Statement;

import ir.Ir;
import ir.Expression.*;

/**
 * @author Nicola
 */
public class IrAssignment extends IrStatement {
    private final IrLocation location;
    private final IrAssignmentOp assignOp;
    private final IrExpression expr;
    
    public enum IrAssignmentOp {
        ASSIGN,
        INC,
        DEC
    }
    
    public IrAssignment(IrLocation location, IrAssignmentOp assignOp, IrExpression expr) {
        this.location = location;
        this.assignOp = assignOp;
        this.expr = expr;
    }
    
    @Override
    public String toString() {
        return assignOp.toString() + "\n" + Ir.indent(location.toString()) + Ir.indent("\n<--\n") + Ir.indent(expr.toString());
    }
    
    
}
