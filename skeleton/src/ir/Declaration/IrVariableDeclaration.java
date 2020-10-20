package ir.Declaration;

import ir.IrVisitor;
import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public class IrVariableDeclaration extends IrMemberDeclaration {
    
    public IrVariableDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}