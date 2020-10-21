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
        
        // Check all fields
        env.beginScope();
        for (IrFieldDeclaration fieldDecl : rootClass.getFields()) {
            check &= fieldDecl.accept(this);
        }
        
        // Check all methods
        for (IrMethodDeclaration methodDecl : rootClass.getMethods()) {
            check &= methodDecl.accept(this);
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
        
        return check;
    }
    
    
    @Override
    public Boolean visit(IrMethodDeclaration methodDecl) {
        boolean check = true;
        
        // Get inside variables
        String methodName = methodDecl.getId();
        TypeDescriptor methodType = methodDecl.getType();
        List<IrParameterDeclaration> pars = methodDecl.getParameters();
        IrBlock methodBody = methodDecl.getBody();
        
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
        
        // Add method parameters to scope
        env.beginScope();
        
        // Check method body
        check &= methodBody.accept(this);
        
        // Add method parameters to scope
        env.endScope();
        
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
