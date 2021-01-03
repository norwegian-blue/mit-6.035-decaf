package ir.Expression;

import java.util.HashSet;
import java.util.Set;

import codegen.Instructions.Literal;

public abstract class IrLiteral extends IrExpression {
    
    protected String value;
    
    public IrLiteral(String value) {
        this.value = value;
    }
       
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean isAtom() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }
    
    @Override
    public Set<IrIdentifier> getUsedVars() {
        return new HashSet<IrIdentifier>();
    }
    
    public Literal getLiteral() {
        throw new Error("Not available");
    }
    
}