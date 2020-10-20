package ir.Statement;

import ir.IrVisitor;

/**
 * @author Nicola
 */
public class IrBreakStatement extends IrStatement { 
    
    @Override
    public String toString() {
        return "BREAK";
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
