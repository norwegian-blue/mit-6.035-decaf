package ir.Statement;

import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrContinueStatement extends IrStatement {
    
    @Override
    public String toString() {
        return "CONTINUE";
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}