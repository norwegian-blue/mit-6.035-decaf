package cfg;

import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

public class CfgCreator implements IrVisitor<CfgNode> {

    @Override
    public CfgNode visit(IrClassDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrFieldDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrMethodDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrParameterDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrVariableDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrBinaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrBooleanLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrStringLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrIdentifier node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrMethodCallExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrUnaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrIntLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrAssignment node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrBlock node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrBreakStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrContinueStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrForStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrIfStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CfgNode visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

}
