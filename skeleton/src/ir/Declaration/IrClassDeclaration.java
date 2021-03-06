package ir.Declaration;

import ir.Ir;
import ir.IrVisitor;

import java.util.List;

/**
 * @author Nicola
 */
public class IrClassDeclaration extends Ir {
    
    private final String className;
    private final List<IrFieldDeclaration> fieldDecl;
    private final List<IrMethodDeclaration> methodDecl;
    
    public IrClassDeclaration (String className, List<IrFieldDeclaration> fieldDecl, List<IrMethodDeclaration> methodDecl) {
        this.className = className;
        this.fieldDecl = fieldDecl;
        this.methodDecl = methodDecl;
    }
    
    public List<IrFieldDeclaration> getFields() {
        return fieldDecl;
    }
    
    public List<IrMethodDeclaration> getMethods() {
        return methodDecl;
    }
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public String toString() {
        String str;
        str = "CLASS " + className + "\nfields:";
        for (IrFieldDeclaration fieldDecl : this.fieldDecl) {
            str += "\n" + Ir.indent(fieldDecl.toString());
        }
        str += "\nmethods:";
        for (IrMethodDeclaration methodDecl : this.methodDecl) {
            str += "\n" + Ir.indent(methodDecl.toString()) + "\n";
        }
        return str;
    }
    
    @Override
    public <T> T accept(IrVisitor<T> v) {
        return v.visit(this);
    }
}