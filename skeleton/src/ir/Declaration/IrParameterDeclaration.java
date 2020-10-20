package ir.Declaration;

import ir.IrVisitor;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public class IrParameterDeclaration extends IrMemberDeclaration {
    
    public IrParameterDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}
