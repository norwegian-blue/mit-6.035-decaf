package ir.Declaration;

import semantic.TypeDescriptor;

/** 
 * @author Nicola
 */
public class IrFieldDeclaration extends IrMemberDeclaration {
    
    public IrFieldDeclaration(TypeDescriptor type, String identifier) {
        super(type, identifier);
    }
    
}