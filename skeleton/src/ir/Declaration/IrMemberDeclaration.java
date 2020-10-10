package ir.Declaration;

import ir.Ir;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public abstract class IrMemberDeclaration extends Ir {
    
    private final TypeDescriptor type;
    private final String identifier;
    
    public IrMemberDeclaration (TypeDescriptor type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }
    
    @Override
    public String toString() {
        return "(" + this.type + ") " + this.identifier;
    }
}
