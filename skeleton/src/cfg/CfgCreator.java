package cfg;

import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

public class CfgCreator implements IrVisitor<CFG> {

    @Override
    public CFG visit(IrClassDeclaration node) {
        throw new Error("IrClassDeclaration does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrFieldDeclaration node) {
        throw new Error("IrFieldDeclaration does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrMethodDeclaration node) {
        CFG methodCFG = CFG.makeSingleNode(new CfgEntryNode());        
        methodCFG.concatenate(node.getBody().accept(this));        
        return methodCFG;    
    }

    @Override
    public CFG visit(IrParameterDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrVariableDeclaration node) {
        return CFG.makeSingleNode(new CfgDeclaration(node));
    }

    @Override
    public CFG visit(IrBinaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrBooleanLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrStringLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrIdentifier node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrMethodCallExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrUnaryExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrIntLiteral node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrAssignment node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrBlock node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrBreakStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrContinueStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrForStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrIfStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CFG visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

}
