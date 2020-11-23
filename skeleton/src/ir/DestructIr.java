package ir;

import ir.Expression.IrExpression;
import ir.Statement.IrBlock;
import ir.Statement.IrStatement;

/**
 * @author Nicola
 */
public class DestructIr {
    
    private IrBlock block;
    private Ir simplifiedIr;
    
    public DestructIr(IrBlock block, Ir simplifiedIr) {
        this.block = block;
        this.simplifiedIr = simplifiedIr;
    }
    
    public DestructIr(Ir simplifiedIr) {
        this.simplifiedIr = simplifiedIr;
        this.block = null;
    }
    
    public IrExpression getSimplifiedExp() {
        if (simplifiedIr.isExp()) {
            return (IrExpression)simplifiedIr;
        } else {
            throw new Error("The simplified Ir is not an expression");
        }
    }
    
    public IrStatement getSimplifiedStm() {
        if (simplifiedIr.isStm()) {
            return (IrStatement)simplifiedIr;
        } else {
            throw new Error("The simplified Ir is not a statement");
        }
    }
    
    public IrBlock getDestructBlock() throws NoSuchFieldException {
        if (block != null) {
            return block;
        } else {
            throw new NoSuchFieldException("No simplified block");
        }
    }

}
