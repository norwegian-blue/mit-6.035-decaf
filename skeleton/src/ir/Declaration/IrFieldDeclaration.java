package ir.Declaration;

import ir.IrVisitor;
import semantic.TypeDescriptor;

/** 
 * @author Nicola
 */
public class IrFieldDeclaration extends IrMemberDeclaration {
    
    public IrFieldDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
    
}