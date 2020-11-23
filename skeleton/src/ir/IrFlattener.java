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
               
//        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
//        List<IrStatement> tempOps = new ArrayList<IrStatement>();
//        TypeDescriptor expType = node.getExpType();
//        IrExpression lhs = node.getLHS();
//        IrExpression rhs = node.getRHS();
//        IrBinaryExpression simplifiedExp;
//        
//        if (lhs.isAtom() && rhs.isAtom()) {
//            return new DestructIr(node);
//        }
//        
//        // Destruct LHS
//        if (!lhs.isAtom()) {
//            DestructIr lhsDestruct = lhs.accept(this);
//            IrBlock lhsBlock;
//            try {
//                lhsBlock = lhsDestruct.getDestructBlock();
//                for (IrVariableDeclaration tmpDecl : lhsBlock.getVarDecl()) {
//                    newTemps.add(tmpDecl);
//                }
//                for (IrStatement tmpStm : lhsBlock.getStatements()) {
//                    tempOps.add(tmpStm);
//                }
//            } catch (NoSuchFieldException e) {
//                // LHS is binary expression of atomic parts
//            }
//
//            // Add temporary variable
//            String tmpName = getTmpName();
//            newTemps.add(new IrVariableDeclaration(lhsDestruct.getSimplifiedExp().getExpType(), tmpName));
//            
//            // Assign lhs subtree to temporary
//            IrIdentifier lhsTmp = new IrIdentifier(tmpName);
//            lhsTmp.setExpType(lhsDestruct.getSimplifiedExp().getExpType());
//            tempOps.add(new IrAssignment(lhsTmp, IrAssignmentOp.ASSIGN, lhsDestruct.getSimplifiedExp()));
//            lhs = lhsTmp;
//        }
//        
//        // Destruct RHS
//        if (!rhs.isAtom()) {
//            DestructIr rhsDestruct = rhs.accept(this);
//            IrBlock rhsBlock;
//            try { 
//                rhsBlock = rhsDestruct.getDestructBlock();
//                for (IrVariableDeclaration tmpDecl : rhsBlock.getVarDecl()) {
//                    newTemps.add(tmpDecl);
//                }
//                for (IrStatement tmpStm : rhsBlock.getStatements()) {
//                    tempOps.add(tmpStm);
//                }
//            } catch(NoSuchFieldException e) {
//                // RHS is binary expression of atomic parts 
//            }
//            
//            // Add temporary variable
//            String tmpName = getTmpName();
//            newTemps.add(new IrVariableDeclaration(rhsDestruct.getSimplifiedExp().getExpType(), tmpName));
//            
//            // Assign lhs subtree to temporary
//            IrIdentifier rhsTmp = new IrIdentifier(tmpName);
//            rhsTmp.setExpType(rhsDestruct.getSimplifiedExp().getExpType());
//            tempOps.add(new IrAssignment(rhsTmp, IrAssignmentOp.ASSIGN, rhsDestruct.getSimplifiedExp()));
//            rhs = rhsTmp;
//        }
//        
//        // Simplify expression
//        simplifiedExp = new IrBinaryExpression(node.getOp(), lhs, rhs);
//        simplifiedExp.setExpType(expType);
//        return new DestructIr(new IrBlock(newTemps, tempOps), simplifiedExp);
        
    }

    @Override
    public DestructIr visit(IrBooleanLiteral node) {
        return new DestructIr(node);
    }

    @Override
    public DestructIr visit(IrCalloutExpression node) {
        // TODO desctruct callout expression
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
        
        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
        List<IrStatement> tempOps = new ArrayList<IrStatement>();
        TypeDescriptor expType = node.getExpType();
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
            expTmp.setExpType(expDestruct.getSimplifiedExp().getExpType());
            tempOps.add(new IrAssignment(expTmp, IrAssignmentOp.ASSIGN, expDestruct.getSimplifiedExp()));
            exp = expTmp;
        }
        
        // Simplify expression
        simplifiedExp = new IrUnaryExpression(node.getOp(), exp);
        simplifiedExp.setExpType(expType);
        return new DestructIr(new IrBlock(newTemps, tempOps), simplifiedExp);
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

//    private DestructIr destructArgs(IrCallExpression node) {
//
//        List<IrVariableDeclaration> newTemps = new ArrayList<IrVariableDeclaration>();
//        List<IrStatement> tempOps = new ArrayList<IrStatement>();
//        TypeDescriptor expType = node.getExpType();
//        
//        // Destruct arguments
//        for (IrExpression arg : node.getArgs()) {
//            
//            // Breakdown argument
//            DestructIr argDestr = arg.accept(this);
//            try {
//                IrBlock argBlock = argDestr.getDestructBlock();
//                for (IrVariableDeclaration tmpDecl : argBlock.getVarDecl()) {
//                    newTemps.add(tmpDecl);
//                }
//                for (IrStatement tmpStm : argBlock.getStatements()) {
//                    tempOps.add(tmpStm);
//                }
//                // TODO set arg to last
//            } catch (NoSuchFieldException e) {
//                // Argument is atomic or base expression
//            }
//            
//            // Add temporary if not atomic
//            if (!arg.isAtom()) {
//                
//                String tmpName = getTmpName();
//                newTemps.add(new IrVariableDeclaration(arg.getExpType(), tmpName));
//                
//                IrIdentifier argTmp = new IrIdentifier(tmpName);
//                argTmp.setExpType(arg.getExpType());
//                tempOps.add(new IrAssignment(argTmp, IrAssignmentOp.ASSIGN, arg));
//                // TODO Set argument
//            }
//        }
//    }
    
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
