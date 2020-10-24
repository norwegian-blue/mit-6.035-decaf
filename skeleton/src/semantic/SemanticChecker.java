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
                       "Variable '" + fieldName + "' already declared in current scope"));
            check = false;
        }
        
        // If array, check size > 0
        if (fieldType.isArray() && fieldType.getLength() < 1) {
            errors.add(new SemanticError(fieldDecl.getLineNum(), fieldDecl.getColNum(),
                      "Array '" + fieldName + "[]' size must be greater than zero"));
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
                       "A variable named '" + methodName + "' is already declared in current scope"));
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
                       "No RETURN statement found within method '" + methodName + "' body"));
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
                       "Variable '" + varName + "' already declared in current scope"));
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
        
        // Check location definition and if integer or boolean
        check &= location.accept(this);
        if (location.getExpType().isArray()) {
            errors.add(new SemanticError(assignment.getLineNum(), assignment.getColNum(),
                    "Assigned variable '" + location.getId() + "' must be a scalar"));
            check = false;
        } else if (!(location.getExpType().equals(BaseTypeDescriptor.INT)) && 
                   !(location.getExpType().equals(BaseTypeDescriptor.BOOL))) {
            errors.add(new SemanticError(assignment.getLineNum(), assignment.getColNum(),
                    "Assigned variable '" + location.getId() + "' must be of type INT or BOOLEAN"));
            check = false;
        }
        
        // Check assignment expression
        check &= exp.accept(this);
        
        // Check type matching
        if ((op != IrAssignmentOp.ASSIGN) && !(location.getExpType().equals(BaseTypeDescriptor.INT)) && 
            !(exp.getExpType().equals(BaseTypeDescriptor.INT))) {
            errors.add(new SemanticError(assignment.getLineNum(), assignment.getColNum(),
                    "Increase/decrease operations must be performed between INT types"));
            check = false;
        } else if (!(location.getExpType().equals(exp.getExpType()))) {
            errors.add(new SemanticError(exp.getLineNum(), exp.getColNum(),
                    "The right hand side expression should be of type " + location.getExpType().toString()));
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
    public Boolean visit(IrBinaryExpression exp) {
        boolean check = true;
        
        IrExpression lhs = exp.getLHS();
        IrExpression rhs = exp.getRHS();
        IrBinaryExpression.BinaryOperator op = exp.getOp();
        
        check &= lhs.accept(this);
        check &= rhs.accept(this);
        
        // Check operation types
        switch (op) {
            // arithmetic or relation
            case PLUS:
            case MINUS:
            case TIMES:
            case DIVIDE:
            case MOD:
            case LT:
            case LE:
            case GT:
            case GE:
                if (!lhs.getExpType().equals(BaseTypeDescriptor.INT)) {
                    errors.add(new SemanticError(lhs.getLineNum(), lhs.getColNum(),
                               "Arithmetic/relation expression must be of type INT"));
                    check = false;
                } 
                if (!rhs.getExpType().equals(BaseTypeDescriptor.INT)) {
                    errors.add(new SemanticError(rhs.getLineNum(), rhs.getColNum(),
                               "Arithmetic/relation expression must be of type INT"));
                    check = false;
                }
                break;           
            
            // connective
            case AND:
            case OR:
                if (!lhs.getExpType().equals(BaseTypeDescriptor.BOOL)) {
                    errors.add(new SemanticError(lhs.getLineNum(), lhs.getColNum(),
                               "Connective expression must be of type BOOL"));
                    check = false;
                } 
                if (!rhs.getExpType().equals(BaseTypeDescriptor.BOOL)) {
                    errors.add(new SemanticError(rhs.getLineNum(), rhs.getColNum(),
                               "Connective expression must be of type BOOL"));
                    check = false;
                }
                break;
                
            // equality
            case EQ:
            case NEQ:
                if ((!lhs.getExpType().equals(rhs.getExpType())) ||
                    ((!lhs.getExpType().equals(BaseTypeDescriptor.BOOL)) &&
                     (!lhs.getExpType().equals(BaseTypeDescriptor.INT)))) {
                    errors.add(new SemanticError(lhs.getLineNum(), lhs.getColNum(),
                               "Left hand side and right hand side expressions must be of the same type (INT/BOOL)"));
                    check = false;
                }
                break;
                
            default:
                throw new Error("Unexpected operator type");            
        }
        
        return check;
    }


    @Override
    public Boolean visit(IrIdentifier location) {
        boolean check = true;
        
        // Check if declared and if not a method identifier
        Descriptor locationDesc;
        TypeDescriptor locationType = BaseTypeDescriptor.undefined;
        try {
            locationDesc = env.get(location.getId());
            if (locationDesc.isMethod()) {
                errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                        "Identifier '" + location.getId() + "' should point to a variable (INT/BOOL) not a method"));
                check = false;
            } else {
                locationType = locationDesc.getType();
            }
        } catch (KeyNotFoundException e) {
            errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                    "Variable '" + location.getId() + "' is not declared"));
            check = false;
        }
        
        // Figure if scalar of array type        
        if (locationType.isArray()) {
            if (location.isArrayElement()) {
                locationType = ((ArrayDescriptor)locationType).getBaseType();
            }
        } else {
            if (location.isArrayElement()) {
                errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                        "Indexing a non array variable '" + location.getId() + "'"));
                check = false;
            }
        }
        
        // Check index expression
        if (location.isArrayElement()) {
            IrExpression ind = location.getInd();
            check &= ind.accept(this);
            if (!ind.getExpType().equals(BaseTypeDescriptor.INT)) {
                errors.add(new SemanticError(location.getLineNum(), location.getColNum(),
                        "Array '" + location.getId() + "' index expression must be of type INT"));
                check = false;
            }                
        }

        location.setExpType(locationType);
        return check;
    }
    
    
    @Override
    public Boolean visit(IrCalloutExpression node) {
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
        return true;
    }
    
    
    @Override
    public Boolean visit(IrStringLiteral node) {
        return true;
    }
    
    
    @Override
    public Boolean visit(IrIntLiteral node) {
        // TODO Range check
        return true;
    }
}
