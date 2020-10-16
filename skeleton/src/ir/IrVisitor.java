package ir;

import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

/**
 * @author Nicola
 */
public abstract class IrVisitor<T> {
    
    public abstract T visit(Ir exp);
    
    // Declarations
    public T visit(IrMemberDeclaration decl) {
        return visit((Ir) decl);
    }      
    
    public T visit(IrClassDeclaration decl) {
        return visit((Ir) decl);
    }
    
    public T visit(IrFieldDeclaration decl) {
        return visit((IrMemberDeclaration) decl);
    }
    
    public T visit(IrMethodDeclaration decl) {
        return visit((IrMemberDeclaration) decl);
    }

    public T visit(IrParameterDeclaration decl) {
        return visit((IrMemberDeclaration) decl);
    }
    
    public T visit(IrVariableDeclaration decl) {
        return visit((IrMemberDeclaration) decl);
    }
    
    
    // Expressions
    public T visit(IrExpression exp) {
        return visit((Ir) exp);
    }
    
    public T visit(IrBinaryExpression exp) {
        return visit((IrExpression) exp);
    }
    
    public T visit(IrIdentifier exp) {
        return visit((IrExpression) exp);
    }
    
    public T visit(IrUnaryExpression exp) {
        return visit((IrExpression) exp);
    }
    
    public T visit(IrCallExpression exp) {
        return visit((IrExpression) exp);
    }
    
    public T visit(IrCalloutExpression exp) {
        return visit((IrCallExpression) exp);
    }
    
    public T visit(IrMethodCallExpression exp) {
        return visit((IrExpression) exp);
    }
        
    public T visit(IrLiteral exp) {
        return visit((IrExpression) exp);
    }
    
    public T visit(IrBooleanLiteral exp) {
        return visit((IrLiteral) exp);
    }
    
    public T visit(IrCharLiteral exp) {
        return visit((IrLiteral) exp);
    }
    
    public T visit(IrIntLiteral exp) {
        return visit((IrLiteral) exp);
    }
    
    
    // Statements
    public T visit(IrStatement exp) {
        return visit((Ir) exp);
    }
    
    public T visit(IrAssignment exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrBlock exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrBreakStatement exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrContinueStatement exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrForStatement exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrIfStatement exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrInvokeStatement exp) {
        return visit((IrStatement) exp);
    }
    
    public T visit(IrReturnStatement exp) {
        return visit((IrStatement) exp);
    }
    
}