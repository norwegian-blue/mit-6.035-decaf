package ir;

import java.util.ArrayList;
import java.util.List;

import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import ir.Statement.IrAssignment.IrAssignmentOp;

/**
 * @author Nicola
 */
public class IrFlattener implements IrVisitor<DestructIr> {
    
    private int tmpNum = 1;

    // Declaration (NOT SUPPORTED)
    @Override
    public DestructIr visit(IrClassDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrFieldDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrMethodDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrParameterDeclaration node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrVariableDeclaration node) {
        throw new Error("Not supported");
    }

    
    // Expressions
    @Override
    public DestructIr visit(IrBinaryExpression node) {
               
        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
        List<IrStatement> tempOps = new ArrayList<IrStatement>();
        IrExpression lhs = node.getLHS();
        IrExpression rhs = node.getRHS();
        IrBinaryExpression simplifiedExp;
        
        if (lhs.isAtom() && rhs.isAtom()) {
            return new DestructIr(node);
        }
        
        // Destruct LHS
        if (!lhs.isAtom()) {
            DestructIr lhsDestruct = lhs.accept(this);
            IrBlock lhsBlock;
            try {
                lhsBlock = lhsDestruct.getDestructBlock();
                for (IrVariableDeclaration tmpDecl : lhsBlock.getVarDecl()) {
                    newTemps.add(tmpDecl);
                }
                for (IrStatement tmpStm : lhsBlock.getStatements()) {
                    tempOps.add(tmpStm);
                }
            } catch (NoSuchFieldException e) {
                // LHS is binary expression of atomic parts
            }

            // Add temporary variable
            String tmpName = getTmpName();
            newTemps.add(new IrVariableDeclaration(lhsDestruct.getSimplifiedExp().getExpType(), tmpName));
            
            // Assign lhs subtree to temporary
            IrIdentifier lhsTmp = new IrIdentifier(tmpName);
            tempOps.add(new IrAssignment(lhsTmp, IrAssignmentOp.ASSIGN, lhsDestruct.getSimplifiedExp()));
            lhs = lhsTmp;
        }
        
        // Destruct RHS
        if (!rhs.isAtom()) {
            DestructIr rhsDestruct = rhs.accept(this);
            IrBlock rhsBlock;
            try { 
                rhsBlock = rhsDestruct.getDestructBlock();
                for (IrVariableDeclaration tmpDecl : rhsBlock.getVarDecl()) {
                    newTemps.add(tmpDecl);
                }
                for (IrStatement tmpStm : rhsBlock.getStatements()) {
                    tempOps.add(tmpStm);
                }
            } catch(NoSuchFieldException e) {
                // RHS is binary expression of atomic parts 
            }
            
            // Add temporary variable
            String tmpName = getTmpName();
            newTemps.add(new IrVariableDeclaration(rhsDestruct.getSimplifiedExp().getExpType(), tmpName));
            
            // Assign lhs subtree to temporary
            IrIdentifier rhsTmp = new IrIdentifier(tmpName);
            tempOps.add(new IrAssignment(rhsTmp, IrAssignmentOp.ASSIGN, rhsDestruct.getSimplifiedExp()));
            rhs = rhsTmp;
        }
        
        // Simplify expression
        simplifiedExp = new IrBinaryExpression(node.getOp(), lhs, rhs);
        return new DestructIr(new IrBlock(newTemps, tempOps), simplifiedExp);
        
    }

    @Override
    public DestructIr visit(IrBooleanLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrCalloutExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DestructIr visit(IrStringLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrIdentifier node) {
        // TODO Auto-generated method stub
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrMethodCallExpression node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DestructIr visit(IrUnaryExpression node) {
        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
        List<IrStatement> tempOps = new ArrayList<IrStatement>();
        IrExpression exp = node.getExp();
        IrUnaryExpression simplifiedExp;
        
        if (exp.isAtom()) {
            return new DestructIr(node);
        }
               
        // Destruct expression
        if (!exp.isAtom()) {
            DestructIr expDestruct = exp.accept(this);
            IrBlock expBlock;
            try { 
                expBlock = expDestruct.getDestructBlock();
            } catch(NoSuchFieldException e) {
                throw new Error("Should not happen for non-atomic child");   
            }
            for (IrVariableDeclaration tmpDecl : expBlock.getVarDecl()) {
                newTemps.add(tmpDecl);
            }
            for (IrStatement tmpStm : expBlock.getStatements()) {
                tempOps.add(tmpStm);
            }
            
            // Add temporary variable
            String tmpName = getTmpName();
            newTemps.add(new IrVariableDeclaration(expDestruct.getSimplifiedExp().getExpType(), tmpName));
            
            // Assign lhs subtree to temporary
            IrIdentifier expTmp = new IrIdentifier(tmpName);
            tempOps.add(new IrAssignment(expTmp, IrAssignmentOp.ASSIGN, expDestruct.getSimplifiedExp()));
            exp = expTmp;
        }
        
        // Simplify expression
        simplifiedExp = new IrUnaryExpression(node.getOp(), exp);
        return new DestructIr(new IrBlock(newTemps, tempOps), simplifiedExp);
    }

    @Override
    public DestructIr visit(IrIntLiteral node) {
        return new DestructIr(node);
    }

    
    // Statements
    @Override
    public DestructIr visit(IrAssignment node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DestructIr visit(IrBlock node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrBreakStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrContinueStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrForStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrIfStatement node) {
        throw new Error("Not supported");
    }

    @Override
    public DestructIr visit(IrInvokeStatement node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DestructIr visit(IrReturnStatement node) {
        throw new Error("Not supported");
        //TODO cleanup if not needed
//        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
//        List<IrStatement> tempOps = new ArrayList<IrStatement>();
//        IrExpression exp = node.getReturnExp();
//        IrExpression simplifiedExp;
//        
//        if (!node.returnsValue() || exp.isAtom()) {
//            return new DestructIr(node);
//        }
//                       
//        // Destruct expression
//        DestructIr expDestruct = exp.accept(this);
//        IrBlock expBlock;
//        try { 
//            expBlock = expDestruct.getDestructBlock();
//        } catch(NoSuchFieldException e) {
//            throw new Error("Should not happen for non-atomic child");   
//        }
//        for (IrVariableDeclaration tmpDecl : expBlock.getVarDecl()) {
//            newTemps.add(tmpDecl);
//        }
//        for (IrStatement tmpStm : expBlock.getStatements()) {
//            tempOps.add(tmpStm);
//        }
//        
//        // Simplify expression
//        simplifiedExp = expDestruct.getSimplifiedExp();
//        return new DestructIr(new IrBlock(newTemps, tempOps), simplifiedExp);
    }
    
    private String getTmpName() {
        return "_tmp" + tmpNum++;
    }

}
