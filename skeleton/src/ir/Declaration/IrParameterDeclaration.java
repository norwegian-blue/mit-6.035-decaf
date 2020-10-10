package ir.Declaration;

import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public class IrParameterDeclaration extends IrMemberDeclaration {
    
    public IrParameterDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
}
