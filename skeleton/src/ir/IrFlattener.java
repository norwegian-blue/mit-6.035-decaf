package ir;

import java.util.ArrayList;
import java.util.List;

import ir.Declaration.*;
import ir.Expression.*;
import ir.Statement.*;
import ir.Statement.IrAssignment.IrAssignmentOp;
import semantic.TypeDescriptor;

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
        
        // Destruct lhs and rhs
        DestructIr lhsDestruct = node.getLHS().accept(this);
        DestructIr rhsDestruct = node.getRHS().accept(this);
        DestructIr blockDestruct = mergeDestructs(lhsDestruct, rhsDestruct);
        
        // Atomize lhs and rhs
        DestructIr leftAtom = atomize(lhsDestruct.getSimplifiedExp());
        blockDestruct = mergeDestructs(blockDestruct, leftAtom);
        
        DestructIr rightAtom = atomize(rhsDestruct.getSimplifiedExp());
        blockDestruct = mergeDestructs(blockDestruct, rightAtom);
        
        IrExpression simplifiedExp = new IrBinaryExpression(node.getOp(), 
                                                            leftAtom.getSimplifiedExp(), 
                                                            rightAtom.getSimplifiedExp());
        simplifiedExp.setExpType(node.getExpType());
        DestructIr nodeDestruct = new DestructIr(simplifiedExp);
        
        return mergeDestructs(blockDestruct, nodeDestruct);        
    }

    @Override
    public DestructIr visit(IrBooleanLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrCalloutExpression node) {
        // TODO destruct callout expression
        return null;
    }

    @Override
    public DestructIr visit(IrStringLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrIdentifier node) {
        // TODO destruct array
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrMethodCallExpression node) {
        // TODO destruct method call expression
        return null;
//        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
//        List<IrStatement> tempOps = new ArrayList<IrStatement>();
//        TypeDescriptor expType = node.getExpType();
//        
//        // Destruct arguments
//        List<DestructIr> argsDestruct = destructArgs(node);
//        for (DestructIr argDestr : argsDestruct) {
//            // Breakdown arguments
//            try {
//                IrBlock argBlock = argDestr.getDestructBlock();
//                for (IrVariableDeclaration tmpDecl : argBlock.getVarDecl()) {
//                    newTemps.add(tmpDecl);
//                }
//                for (IrStatement tmpStm : argBlock.getStatements()) {
//                    tempOps.add(tmpStm);
//                }
//            } catch (NoSuchFieldException e) {
//                continue;
//            }
//        }
//
//        if (!node.returnsVal()) {
//            return new DestructIr(node);
//        }
//        
        //
    }

    @Override
    public DestructIr visit(IrUnaryExpression node) {  
        
        // Destruct exp
        DestructIr expDestruct = node.getExp().accept(this);
        
        // Atomize exp
        DestructIr expAtom = atomize(expDestruct.getSimplifiedExp());
        expDestruct = mergeDestructs(expDestruct, expAtom);
        
        IrExpression simplifiedExp = new IrUnaryExpression(node.getOp(), expAtom.getSimplifiedExp());
        simplifiedExp.setExpType(node.getExpType());
        DestructIr nodeDestruct = new DestructIr(simplifiedExp);
        
        return mergeDestructs(expDestruct, nodeDestruct);
    }

    @Override
    public DestructIr visit(IrIntLiteral node) {
        return new DestructIr(node);
    }

    
    // Statements
    @Override
    public DestructIr visit(IrAssignment node) {
        // TODO destruct assignment
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
        // TODO destruct invoke statement
        return null;
    }

    @Override
    public DestructIr visit(IrReturnStatement node) {
        throw new Error("Not supported");
    }
    
    
    public String getTmpName() {
        return "_tmp" + tmpNum++;
    }
    
    private DestructIr mergeDestructs(DestructIr destr1, DestructIr destr2) {
        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
        List<IrStatement> tempOps = new ArrayList<IrStatement>();
        
        // Add destr1
        try { 
            IrBlock block = destr1.getDestructBlock();
            for (IrVariableDeclaration tmpDecl : block.getVarDecl()) {
                newTemps.add(tmpDecl);
            }
            for (IrStatement tmpStm : block.getStatements()) {
                tempOps.add(tmpStm);
            }
        } catch(NoSuchFieldException e) {
            // destr1 is atomic
        }
        
        // Add destr2
        try { 
            IrBlock block = destr2.getDestructBlock();
            for (IrVariableDeclaration tmpDecl : block.getVarDecl()) {
                newTemps.add(tmpDecl);
            }
            for (IrStatement tmpStm : block.getStatements()) {
                tempOps.add(tmpStm);
            }
        } catch(NoSuchFieldException e) {
            // destr2 is atomic
        }
        
        return new DestructIr(new IrBlock(newTemps, tempOps), destr2.getSimplifiedExp());
        
    }
    
    private DestructIr atomize(IrExpression exp) {
        // Return atom if atomic
        if (exp.isAtom()) {
            return new DestructIr(exp);
        }
        
        // Create atomic identifier (temporary variable)
        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
        List<IrStatement> tempOps = new ArrayList<IrStatement>();
        
        String tmpName = getTmpName();
        newTemps.add(new IrVariableDeclaration(exp.getExpType(), tmpName));
        
        IrIdentifier expTmp = new IrIdentifier(tmpName);
        expTmp.setExpType(exp.getExpType());
        tempOps.add(new IrAssignment(expTmp, IrAssignmentOp.ASSIGN, exp));

        
        return new DestructIr(new IrBlock(newTemps, tempOps), expTmp);
        
    }
    
}
