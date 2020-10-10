package ir.Declaration;

import semantic.TypeDescriptor;

/**
 * @author Nicola
 */
public class IrVariableDeclaration extends IrMemberDeclaration {
    
    public IrVariableDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
}