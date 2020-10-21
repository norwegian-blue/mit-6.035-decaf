package ir;

import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

/**
 * @author Nicola
 */
public interface IrVisitor<T> {
    
    // Declarations
    public T visit(IrClassDeclaration node);
    
    public T visit(IrFieldDeclaration node);
    
    public T visit(IrMethodDeclaration node);
    
    public T visit(IrParameterDeclaration node);
    
    public T visit(IrVariableDeclaration node);
    
    // Expressions
    public T visit(IrBinaryExpression node);
    
    public T visit(IrBooleanLiteral node);
    
    public T visit(IrCalloutExpression node);
    
    public T visit(IrCharLiteral node);
    
    public T visit(IrIdentifier node);
    
    public T visit(IrMethodCallExpression node);
    
    public T visit(IrUnaryExpression node);
    
    public T visit(IrIntLiteral node);
    
    // Statements
    public T visit(IrAssignment node);
    
    public T visit(IrBlock node);
    
    public T visit(IrBreakStatement node);
    
    public T visit(IrContinueStatement node);
    
    public T visit(IrForStatement node);
    
    public T visit(IrIfStatement node);
    
    public T visit(IrInvokeStatement node);
    
    public T visit(IrReturnStatement node);

}
