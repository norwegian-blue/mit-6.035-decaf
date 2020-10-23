package semantic;

import java.util.ArrayList;
import java.util.List;
import ir.*;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import ir.Statement.IrAssignment.IrAssignmentOp;

/**
 * @author Nicola
 */
public class SemanticChecker implements IrVisitor<Boolean> {
    
    private final SymbolTable env;
    private final List<SemanticError> errors;
    private boolean looping = false;
    private boolean needReturn = false;
    private MethodDescriptor currentMethod;
    
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
                       "No main method is defined"));
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
        
        // Mark for needed return value
        if (methodType != BaseTypeDescriptor.VOID) {
            needReturn = true;
        }
        
        // Check if main method
        if (methodName.equals("main")) {
            if (methodType != BaseTypeDescriptor.VOID) {
                errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                        "Main method must not return a value"));
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
        MethodDescriptor thisMethod = new MethodDescriptor(methodName, methodType, methodPars);
        try {
            env.put(methodName, thisMethod);
        } catch (DuplicateKeyException e) {
            errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                       "Method " + methodName + " already declared in current scope"));
            check = false;
        }
        
        
        // Add method parameters to scope
        env.beginScope();
        for (IrParameterDeclaration par : pars) {
            try {
                env.put(par.getId(), new ParameterDescriptor(par.getId(), par.getType()));
            } catch (DuplicateKeyException e) {
                throw new Error("Unexpected exception");
            }
        }
        currentMethod = thisMethod;
        
        // Check method body
        check &= methodBody.accept(this);
        
        // Pop method scope from environment
        env.endScope();
        
        // Check if method returned value when needed
        if (needReturn) {
            errors.add(new SemanticError(methodDecl.getLineNum(), methodDecl.getColNum(),
                       "No RETURN statement found within method " + methodName + " body"));
            check = false;
        }
        needReturn = false;
            
        
        return check;
    }
    

    @Override
    public Boolean visit(IrParameterDeclaration node) {
        return true;
    }
    
    
    @Override
    public Boolean visit(IrBlock block) {
        boolean check = true;
        
        env.beginScope();
        
        // Check all variable declarations
        for (IrVariableDeclaration varDecl : block.getVarDecl()) {
            check &= varDecl.accept(this);
        }
        
        // Check all statements
        for (IrStatement statement : block.getStatements()) {
            check &= statement.accept(this);
        }
        
        env.endScope();
        return check;
    }
    

    @Override
    public Boolean visit(IrVariableDeclaration varDecl) {
        boolean check = true;
        
        // Add to environment (check if already defined)
        String varName = varDecl.getId();
        TypeDescriptor fieldType = varDecl.getType();
        try {
            env.put(varName, new LocalDescriptor(varName, fieldType));
        } catch (DuplicateKeyException e) {
            errors.add(new SemanticError(varDecl.getLineNum(), varDecl.getColNum(),
                       "Variable " + varName + " already declared in current scope"));
            check = false;
        }
        return check;
    }
    
    
    @Override
    public Boolean visit(IrAssignment assignment) {
        boolean check = true;
        
        IrIdentifier location = assignment.getLocation();
        IrExpression exp = assignment.getExpression();
        IrAssignmentOp op = assignment.getOp();
        
        // Check if location is declared and maps to variable
        Descriptor locationDesc;
        TypeDescriptor locationType = BaseTypeDescriptor.unassigned;
        try {
            locationDesc = env.get(location.getId());
            if (locationDesc.isMethod()) {
                errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                        "Identifier " + location.getId() + " should point to a variable (int/boolean) not a method"));
                check = false;
            }
            locationType = locationDesc.getType();
        } catch (KeyNotFoundException e) {
            errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                    "Variable " + location.getId() + " is not declared"));
            check = false;
        }
        
        // Check if scalar assignment
        if (locationType.isArray()) {
            if (location.isArray()) {
                locationType = ((ArrayDescriptor)locationType).getBaseType();
            } else {
                errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                        "Assignment variable " + location.getId() + " must be a scalar"));
                check = false;
            }
        }
        
        // Check if integer or boolean location
        if (check && (locationType!=BaseTypeDescriptor.BOOL) && (locationType!=BaseTypeDescriptor.INT)) {
            errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                    "Assignment variable " + location.getId() + " must be a an integer or a boolean"));
            check = false;
        }
        
        return check;
    }
    
    
    @Override
    public Boolean visit(IrBreakStatement node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrContinueStatement node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrForStatement node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrIfStatement node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        needReturn = false;
        return true;
    }
      

    @Override
    public Boolean visit(IrBinaryExpression node) {
        // TODO Auto-generated method stub
        return true;
    }



    @Override
    public Boolean visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return true;
    }



    @Override
    public Boolean visit(IrIdentifier node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrMethodCallExpression node) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean visit(IrUnaryExpression node) {
        // TODO Auto-generated method stub
        return true;
    }




    
    @Override
    public Boolean visit(IrBooleanLiteral node) {
        // TODO Auto-generated method stub
        return true;
    }
    
    
    @Override
    public Boolean visit(IrCharLiteral node) {
        // TODO Auto-generated method stub
        return true;
    }
    
    @Override
    public Boolean visit(IrIntLiteral node) {
        // TODO Auto-generated method stub
        return true;
    }
}
