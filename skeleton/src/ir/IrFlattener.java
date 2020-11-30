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
        return destructCall(node);
    }

    @Override
    public DestructIr visit(IrStringLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrIdentifier node) {
        if (node.isAtom()) {
            return new DestructIr(node);
        }
        
        // Destruct and atomize index expression
        DestructIr destructInd = node.getInd().accept(this);
        DestructIr atomInd = atomize(destructInd.getSimplifiedExp());
        destructInd = mergeDestructs(destructInd, atomInd);
        
        IrIdentifier newNode = new IrIdentifier(node.getId(), atomInd.getSimplifiedExp());
        newNode.setExpType(node.getExpType());
        DestructIr destructNode = new DestructIr(newNode);
        return mergeDestructs(destructInd, destructNode);
    }

    @Override
    public DestructIr visit(IrMethodCallExpression node) {
        return destructCall(node);
    }
    
    private DestructIr destructCall(IrCallExpression node) {
        
        DestructIr callDestruct = new DestructIr(node);
        int i = 0;
        
        // Destruct and atomize arguments   
        for (IrExpression arg : node.getArgs()) {
            // Destruct argument
            DestructIr argDestruct = arg.accept(this);
            callDestruct = mergeDestructs(callDestruct, argDestruct);
            
            // Atomize argument
            DestructIr argAtom = atomize(argDestruct.getSimplifiedExp());
            callDestruct = mergeDestructs(callDestruct, argAtom);
            node.getArgs().set(i, argAtom.getSimplifiedExp());
            i++;
        }
        
        DestructIr updatedCall = new DestructIr(node);
        callDestruct = mergeDestructs(callDestruct, updatedCall);
        return callDestruct;
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
        
        // Break down INC / DEC assignments
        if (node.getOp() == IrAssignment.IrAssignmentOp.INC) {
            IrExpression newExp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.PLUS,
                                                         node.getLocation(),
                                                         node.getExpression());
            newExp.setExpType(node.getExpression().getExpType());
            IrAssignment newAssign = new IrAssignment(node.getLocation(),
                                                      IrAssignment.IrAssignmentOp.ASSIGN,
                                                      newExp);
            return newAssign.accept(this);
        } else if (node.getOp() == IrAssignment.IrAssignmentOp.DEC) {
            IrExpression newExp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.MINUS,
                                                         node.getLocation(),
                                                         node.getExpression());
            newExp.setExpType(node.getExpression().getExpType());
            IrAssignment newAssign = new IrAssignment(node.getLocation(),
                                                      IrAssignment.IrAssignmentOp.ASSIGN,
                                                      newExp);
            return newAssign.accept(this);
        }
        
        // Destruct location and assign expression
        DestructIr locationDestruct = node.getLocation().accept(this);
        DestructIr expDestruct = node.getExpression().accept(this);
        DestructIr blockDestruct = mergeDestructs(locationDestruct, expDestruct);
        
        // Atomize expression
        DestructIr expAtom = atomize(expDestruct.getSimplifiedExp());
        blockDestruct = mergeDestructs(blockDestruct, expAtom);
        
        IrAssignment newAssign = new IrAssignment((IrIdentifier)locationDestruct.getSimplifiedExp(), 
                                                  node.getOp(), expAtom.getSimplifiedExp());
        
        try {
            IrBlock block = blockDestruct.getDestructBlock();
            return new DestructIr(block, newAssign);
        } catch (NoSuchFieldException e) {
            return new DestructIr(newAssign);
        }
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
        DestructIr destructInvoke = node.getMethod().accept(this);
        
        try {
            IrBlock destructBlock = destructInvoke.getDestructBlock();
            return new DestructIr(destructBlock, node);
        } catch (NoSuchFieldException e) {
            return new DestructIr(node);
        }
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
