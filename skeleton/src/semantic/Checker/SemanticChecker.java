package semantic.Checker;

import java.util.ArrayList;
import java.util.List;
import ir.*;
import ir.Declaration.*;
import semantic.*;

/**
 * @author Nicola
 */
public class SemanticChecker extends IrVisitor<SemanticError> {
    
    private final SymbolTable env;
    private final List<SemanticError> errors;
    
    public SemanticChecker() {
        this.env = new SymbolTable();
        this.errors = new ArrayList<>();
    }
    
    @Override
    public SemanticError visit(IrClassDeclaration rootClass) {
        
        // Check all fields
        env.beginScope();
        for (IrFieldDeclaration fieldDecl : rootClass.getFields()) {
            fieldDecl.accept(this);
        }
        
        System.out.print(env.toString());
        
        // Check all methods
        for (IrMethodDeclaration methodDecl : rootClass.getMethods()) {
            methodDecl.accept(this);
        }
        
        return SemanticError.NoError;
    }
    
    @Override
    public SemanticError visit(IrFieldDeclaration fieldDecl) {
        
        // Check array length
        String fieldName = fieldDecl.getId();
        TypeDescriptor fieldType = fieldDecl.getType();
        if (fieldType.isArray() && fieldType.getLength() < 1) {
            errors.add(new SemanticError(fieldDecl.getLineNum(), fieldDecl.getColNum(),
                                         "Array size", 
                                         fieldName + " array lenght must be higher than 0"));
        }
            
        // Add to environment
        env.put(fieldName, new FieldDescriptor(fieldName, fieldType));
        
        return SemanticError.NoError;
    }
    
    @Override
    public SemanticError visit(IrMethodDeclaration methodDecl) {
        return SemanticError.NoError;
    }

    @Override
    public SemanticError visit(Ir exp) {
        // TODO Auto-generated method stub
        return null;
    }
}
