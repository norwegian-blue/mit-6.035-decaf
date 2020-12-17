package ir.Statement;

import ir.Ir;

/**
 * @author Nicola
 */
public abstract class IrStatement extends Ir {
    
    @Override
    public boolean isStm() {
        return true;
    }
    
    public boolean isAssignment() {
        return false;
    }
    
    public boolean isInvokeStatement() {
        return false;
    }
}