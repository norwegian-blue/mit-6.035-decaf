package ir.Declaration;

import ir.Ir;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Nicola
 */
public class IrClassDeclaration extends Ir {
    
    private final String name;
    private final List<IrFieldDeclaration> fieldDecl;
    private final List<IrMethodDeclaration> methodDecl;
    
    public IrClassDeclaration (String name, List<IrFieldDeclaration> fieldDecl, List<IrMethodDeclaration> methodDecl) {
        this.name = name;
        this.fieldDecl = fieldDecl;
        this.methodDecl = methodDecl;
    }
}