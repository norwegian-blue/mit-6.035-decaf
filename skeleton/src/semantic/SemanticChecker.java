package semantic;

import java.util.ArrayList;
import java.util.List;
import ir.*;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

/**
 * @author Nicola
 */
public class SemanticChecker implements IrVisitor<Boolean> {
    
    private final SymbolTable env;
    private final List<SemanticError> errors;
    
    public SemanticChecker() {
        this.env = new SymbolTable();
        this.errors = new ArrayList<>();
    }
    
    /**
     * Print all errors found by the semantic checker
     */
    public void printErrors() {
        for (SemanticError err : errors) {
            System.out.println(err.toString());
        }
    }
    
    
    @Override
    public Boolean visit(IrClassDeclaration rootClass) {
        
        boolean check = true;
        boolean checkMain = false;
        
        // Check all fields
        env.beginScope();
        for (IrFieldDeclaration fieldDecl : rootClass.getFields()) {
            check &= fieldDecl.accept(this);
        }
        
        // Check all methods
        for (IrMethodDeclaration methodDecl : rootClass.getMethods()) {
            check &= methodDecl.accept(this);
            if (methodDecl.getId().equals("main")) {
                checkMain = true;
            }
        }
        
        // Check if main method is defined
        if (!checkMain) {
            errors.add(new SemanticError(rootClass.getLineNum(), rootClass.getColNum(),
                       "A main method must be defined"));
            check = false;
        }            
        
        return check;
    }
    
    
    @Override
    public Boolean visit(IrFieldDeclaration fieldDecl) {
        
        boolean check = true;
        
        // Add to environment (check if already defined)
        String fieldName = fieldDecl.getId();
        TypeDescriptor fieldType = fieldDecl.getType();
        try {
            env.put(fieldName, new FieldDescriptor(fieldName, fieldType));
        } catch (DuplicateKeyException e) {
            errors.add(new SemanticError(fieldDecl.getLineNum(), fieldDecl.getColNum(),
                       "Variable " + fieldName + " already declared in current scope"));
            check = false;
        }
        
        // If array, check size > 0
        if (fieldType.isArray() && fieldType.getLength() < 1) {
            errors.add(new SemanticError(fieldDecl.getLineNum(), fieldDecl.getColNum(),
                      "Array " + fieldName + "[] size must be higher than zero"));
            check = false;
        }
        
        return check;
    }
    
    
    @Override
    public Boolean visit(IrMethodDeclaration methodDecl) {
        boolean check = true;
        
        // Get method fields
        String methodName = methodDecl.getId();
        TypeDescriptor methodType = methodDecl.getType();
        List<IrParameterDeclaration> pars = methodDecl.getParameters();
        IrBlock methodBody = methodDecl.getBody();
        
        // Check if main method
        if (methodName.equals("main")) {
            if (methodType != BaseTypeDescriptor.VOID) {
                errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                        "Main method must not return any value"));
                check = false;
            } else if (pars.size() > 0) {
                errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                        "Main method must not take any parameter"));
                check = false;
            }
        }
        
        // Add method to environment (check if already defined)
        List<ParameterDescriptor> methodPars = new ArrayList<>();
        for (IrParameterDeclaration par : pars) {
            methodPars.add(new ParameterDescriptor(par.getId(), par.getType()));
        }            
        try {
            env.put(methodName, new MethodDescriptor(methodName, methodType, methodPars));
        } catch (DuplicateKeyException e) {
            errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                       "Method " + methodName + " already declared in current scope"));
            check = false;
        }
        
        System.out.println(env.toString());
        
        // Add method parameters to scope
        env.beginScope();
        for (IrParameterDeclaration par : pars) {
            try {
                env.put(par.getId(), new ParameterDescriptor(par.getId(), par.getType()));
            } catch (DuplicateKeyException e) {
                throw new Error("Unexpected exception");
            }
        }
        
        System.out.println(env.toString());
        
        // Check method body
        check &= methodBody.accept(this);
        
        // Add method parameters to scope
        env.endScope();
        System.out.println(env.toString());
        
        return check;
    }
    

    @Override
    public Boolean visit(IrParameterDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrVariableDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrBinaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrBooleanLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrCharLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrIdentifier node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrMethodCallExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrUnaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrIntLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrAssignment node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrBlock node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrBreakStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrContinueStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrForStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrIfStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
}
