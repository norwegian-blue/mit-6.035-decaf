package ir;

import java.util.List;

import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import semantic.*;

/**
 * @author Nicola
 */

public class IrRenamer implements IrVisitor<Ir> {
    
    private static int globalCount = 0;
    private static int localCount = 0;
    private final SymbolTable env = new SymbolTable();
    private static String currentMethod;
    
    @Override
    public Ir visit(IrClassDeclaration node) {
        
        env.beginScope();
        
        // Rename globals
        List<IrFieldDeclaration> fields = node.getFields();
        for (int i = 0; i < fields.size(); i++) {
            fields.set(i, (IrFieldDeclaration) fields.get(i).accept(this));
        }
        
        // Rename methods
        List<IrMethodDeclaration> methods = node.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            methods.set(i, (IrMethodDeclaration) methods.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrFieldDeclaration node) {
        
        String fieldName = node.getId();
        TypeDescriptor fieldType = node.getType();
        FieldDescriptor desc = new FieldDescriptor(fieldName, fieldType);
        String alias = getGlobalAlias(fieldName);
        desc.setAlias(alias);
        
        try {
            env.put(node.getId(), desc);
        } catch (DuplicateKeyException e) {
            throw new Error("Unexpected error after semantic check");
        }
        
        IrFieldDeclaration newNode = new IrFieldDeclaration(fieldType, alias);
        return newNode;
    }

    @Override
    public Ir visit(IrMethodDeclaration node) {
        
        currentMethod = node.getId();
        localCount = 0;
        env.beginScope();
        
        List<IrParameterDeclaration> parameters = node.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            parameters.set(i, (IrParameterDeclaration) parameters.get(i).accept(this));
        }
        
        IrBlock block = node.getBody();
        block = (IrBlock) block.accept(this);
        
        env.endScope();
        
        IrMethodDeclaration newNode = new IrMethodDeclaration(node.getId(), node.getType(), parameters, block);
        return newNode;
    }

    @Override
    public Ir visit(IrParameterDeclaration node) {
        String varName = node.getId();
        TypeDescriptor varType = node.getType();
        FieldDescriptor desc = new FieldDescriptor(varName, varType);
        String alias = getLocalAlias(varName);
        desc.setAlias(alias);
        
        try {
            env.put(node.getId(), desc);
        } catch (DuplicateKeyException e) {
            throw new Error("Unexpected error after semantic check");
        }
        
        IrParameterDeclaration newNode = new IrParameterDeclaration(varType, alias);
        return newNode;
    }

    @Override
    public Ir visit(IrVariableDeclaration node) {
        
        String varName = node.getId();
        TypeDescriptor varType = node.getType();
        FieldDescriptor desc = new FieldDescriptor(varName, varType);
        String alias = getLocalAlias(varName);
        desc.setAlias(alias);
        
        try {
            env.put(node.getId(), desc);
        } catch (DuplicateKeyException e) {
            throw new Error("Unexpected error after semantic check");
        }
        
        IrVariableDeclaration newNode = new IrVariableDeclaration(varType, alias);
        return newNode;
    }

    @Override
    public Ir visit(IrBinaryExpression node) {
        IrBinaryExpression newNode = new IrBinaryExpression(node.getOp(),
                (IrExpression) node.getLHS().accept(this), 
                (IrExpression) node.getRHS().accept(this));
        return newNode;
    }

    @Override
    public Ir visit(IrBooleanLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrCalloutExpression node) {
        
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, (IrExpression) args.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrStringLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrIdentifier node) {
        
        String varName = node.getId();
        String newName;
        try {
            newName = env.get(varName).getAlias();
        } catch (KeyNotFoundException e) {
            throw new Error("Unexpected error after semantic check");
        }
        
        IrIdentifier newNode;
        if (node.isArrayElement()) {
            newNode = new IrIdentifier(newName, (IrExpression) node.getInd().accept(this));
            
        } else {
            newNode = new IrIdentifier(newName);
        }
        return newNode;
    }

    @Override
    public Ir visit(IrMethodCallExpression node) {
        List<IrExpression> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            args.set(i, (IrExpression) args.get(i).accept(this));
        }
        
        return node;
    }

    @Override
    public Ir visit(IrUnaryExpression node) {
        Ir newNode = new IrUnaryExpression(node.getOp(), (IrExpression) node.getExp().accept(this));
        return newNode;
    }

    @Override
    public Ir visit(IrIntLiteral node) {
        return node;
    }

    @Override
    public Ir visit(IrAssignment node) {
        IrAssignment newNode = new IrAssignment((IrIdentifier) node.getLocation().accept(this),
                                                node.getOp(), 
                                                (IrExpression) node.getExpression().accept(this));
        return newNode;
    }

    @Override
    public Ir visit(IrBlock node) {
        env.beginScope();
        
        List<IrVariableDeclaration> vars = node.getVarDecl();
        for (int i = 0; i < vars.size(); i++) {
            vars.set(i, (IrVariableDeclaration) vars.get(i).accept(this));
        }
        
        List<IrStatement> statements = node.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            statements.set(i, (IrStatement) statements.get(i).accept(this));
        }
        
        env.endScope();
        
        return node;
    }

    @Override
    public Ir visit(IrBreakStatement node) {
        return node;
    }

    @Override
    public Ir visit(IrContinueStatement node) {
        return node;
    }

    @Override
    public Ir visit(IrForStatement node) {
        
        env.beginScope();
        
        IrIdentifier loopVar = node.getLoopVar();
        LocalDescriptor loopDesc = new LocalDescriptor(loopVar.getId(), loopVar.getExpType());
        loopDesc.setAlias(getLocalAlias(loopVar.getId()));
        
        try {
            env.put(loopVar.getId(), loopDesc);
        } catch (DuplicateKeyException e) {
            throw new Error("Unexpected error after semantic check");
        }
        
        IrForStatement newNode = new IrForStatement((IrIdentifier) node.getLoopVar().accept(this),
                (IrExpression) node.getStartExp().accept(this),
                (IrExpression) node.getEndExp().accept(this),
                (IrBlock) node.getLoopBlock().accept(this));
        
        env.endScope();
        
        return newNode;
    }

    @Override
    public Ir visit(IrIfStatement node) {
        IrIfStatement newNode = new IrIfStatement((IrExpression) node.getCondition().accept(this),
                (IrBlock) node.getThenBlock().accept(this), 
                (IrBlock) node.getElseBlock().accept(this));

        return newNode;
    }

    @Override
    public Ir visit(IrInvokeStatement node) {
        IrInvokeStatement newNode = new IrInvokeStatement((IrCallExpression) node.getMethod().accept(this));
        return newNode;
    }

    @Override
    public Ir visit(IrReturnStatement node) {
        if (node.returnsVoid()) {
            return node;
        } else {
            IrReturnStatement newNode = new IrReturnStatement((IrExpression) node.getReturnExp().accept(this));
            return newNode;
        }
    }    
    
    private static String getGlobalAlias(String name) {
        globalCount += 1;
        return "_glb" + globalCount + "_" + name;
    }
    
    private static String getLocalAlias(String name) {
        localCount += 1;
        return "_" + currentMethod + localCount + "_" + name;
    }

}
