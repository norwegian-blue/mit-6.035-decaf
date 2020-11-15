package cfg;

import ir.IrVisitor;
import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;

public class CfgCreator implements IrVisitor<CFG> {
    
    // Declarations

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

    
    // Expressions
    
    @Override
    public CFG visit(IrBinaryExpression node) {
        throw new Error("IrBinaryExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrBooleanLiteral node) {
        throw new Error("IrBooleanLiteral does not serve CfgCreator");

    }

    @Override
    public CFG visit(IrCalloutExpression node) {
        throw new Error("IrCalloutExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrStringLiteral node) {
        throw new Error("IrStringLiteral does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrIdentifier node) {
        throw new Error("IrIdentifier does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrMethodCallExpression node) {
        throw new Error("IrMethodCallExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrUnaryExpression node) {
        throw new Error("IrUnaryExpression does not serve CfgCreator");
    }

    @Override
    public CFG visit(IrIntLiteral node) {
        throw new Error("IrIntLiteral does not serve CfgCreator");
    }
    
    
    // Statements

    @Override
    public CFG visit(IrAssignment node) {
        return CFG.makeSingleNode(new CfgStatement(node));
    }

    @Override
    public CFG visit(IrBlock node) {
        
        CFG block = CFG.makeNoOp();
        
        for (IrVariableDeclaration decl : node.getVarDecl()) {
            block.concatenate(new CfgDeclaration (decl));
        }
        
        for (IrStatement stat : node.getStatements()) {
            block.concatenate(stat.accept(this));
        }
        
        return block;
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
        CFG ifBranch = node.getThenBlock().accept(this);
        CFG elseBranch = node.getElseBlock().accept(this);
        CfgNoOp noOp = new CfgNoOp();
        
        ifBranch.concatenate(noOp);
        elseBranch.concatenate(noOp);
        
        return shortCircuit(node.getCondition(), ifBranch, elseBranch);
        
    }

    @Override
    public CFG visit(IrInvokeStatement node) {
        return CFG.makeSingleNode(new CfgStatement(node));
    }

    @Override
    public CFG visit(IrReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    // Private

    private CFG shortCircuit(IrExpression cond, CFG ifBranch, CFG elseBranch) {
        
        CFG graph = CFG.makeNoOp();
        
        if (cond.isAndExp()) {
            // TODO recursive shortCircuit
        } else if (cond.isOrExp()) {
            // TODO recursive shortCircuit
        } else if (cond.isNotExp()) {
            // TODO recursive shortCircuit
        } else {
            CfgBranch cfgCond = new CfgBranch(cond);
            graph.concatenate(cfgCond);
            //TODO connect branches
        }
        
        return graph;
    }
    
}
