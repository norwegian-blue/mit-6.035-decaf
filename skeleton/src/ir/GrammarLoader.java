package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ir.Expression.*;
import ir.Statement.*;
import semantic.*;
import ir.Declaration.*;
import decaf.*;
import decaf.GrammarParser.*;


/**
 * @author Nicola
 */
public class GrammarLoader extends GrammarBaseListener {
    private Stack<Ir> stack = new Stack<Ir>();
    
    /**
     * Return the expression constructed by the listener during 
     * the tree walk. The listener should have walked the entire Grammar Parse tree
     * @return Ir with AST of the parsed program
     */
    public Ir getAbstractSyntaxTree() {
        return stack.pop();
    }
    
    @Override 
    public void exitField_decl(GrammarParser.Field_declContext ctx) {
        TypeDescriptor baseFieldType;
        List<IrFieldDeclaration> fieldDeclList = new ArrayList<>();
        
        if (ctx.type().TK_BOOL() != null) {
            baseFieldType = TypeDescriptor.BOOL;
        } else if (ctx.type().TK_INT() != null) {
            baseFieldType = TypeDescriptor.INT;
        } else {
            throw new RuntimeException("cannot identify type");
        }
        
        for (int i = 0; i < ctx.field().size(); i++) {
            TypeDescriptor fieldType;
            IrFieldDeclaration fieldDecl;
            String fieldName = ctx.field(i).getText();
            
            if (ctx.field(i).INT_LITERAL() == null) {
                fieldType = baseFieldType;
            } else {
                fieldType = new ArrayDescriptor(baseFieldType, Integer.parseInt(ctx.field(i).INT_LITERAL().getText()));
            }
            
            fieldDecl = new IrFieldDeclaration(fieldType, fieldName);
            fieldDecl.setLineNum(ctx.getStart().getLine());
            fieldDecl.setColNum(ctx.getStart().getCharPositionInLine());
            fieldDeclList.add(fieldDecl);
        }
        
        Collections.reverse(fieldDeclList);
        for (IrFieldDeclaration field : fieldDeclList) {
            stack.push(field);
        }
        
    }
    
    @Override
    public void exitMethod_decl(GrammarParser.Method_declContext ctx) {
        List<IrParameterDeclaration> methodPars = new ArrayList<>();
        String methodName;
        IrBlock methodBody;
        TypeDescriptor methodType;
        
        methodName = ctx.ID().getText();
        methodBody = (IrBlock) stack.pop();
        
        if (ctx.TK_VOID() != null) {
            methodType = TypeDescriptor.VOID;
        } else if (ctx.type().TK_BOOL() != null) {
            methodType = TypeDescriptor.BOOL;
        } else if (ctx.type().TK_INT() != null){
            methodType = TypeDescriptor.INT;
        } else {
            throw new RuntimeException("cannot identify type");
        }
        
        for (int i = 0; i < ctx.method_par().type().size(); i++) {
            methodPars.add((IrParameterDeclaration) stack.pop());
        }
        Collections.reverse(methodPars);
        
        IrMethodDeclaration method = new IrMethodDeclaration(methodName, methodType, methodPars, methodBody);
        method.setLineNum(ctx.getStart().getLine());
        method.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(method);
    }
    
    @Override 
    public void exitMethod_par(GrammarParser.Method_parContext ctx) {
        for (int i = 0; i < ctx.ID().size(); i++) {
            String parName = ctx.ID(i).getText();
            TypeDescriptor parType;
            if (ctx.type(i).TK_BOOL() != null) {
                parType = TypeDescriptor.BOOL;
            } else if (ctx.type(i).TK_INT() != null) {
                parType = TypeDescriptor.INT;
            } else {
                throw new RuntimeException("cannot identify type");
            }
            IrParameterDeclaration par = new IrParameterDeclaration(parType, parName);
            par.setLineNum(ctx.getStart().getLine());
            par.setColNum(ctx.getStart().getCharPositionInLine());
            stack.push(par);
        }        
    }
    
    @Override
    public void exitVar_decl(GrammarParser.Var_declContext ctx) {
        for (int i = 0; i < ctx.ID().size(); i++) {
            String varName = ctx.ID(i).getText();
            TypeDescriptor varType;
            if (ctx.type(i).TK_BOOL() != null) {
                varType = TypeDescriptor.BOOL;
            } else if (ctx.type(i).TK_INT() != null) {
                varType = TypeDescriptor.INT;
            } else {
                throw new RuntimeException("cannot identify type");
            }
            IrVariableDeclaration var = new IrVariableDeclaration(varType, varName);
            var.setLineNum(ctx.getStart().getLine());
            var.setColNum(ctx.getStart().getCharPositionInLine());
            stack.push(var);
        }
    }     
        
    @Override
    public void exitBlock(GrammarParser.BlockContext ctx) {
        List<IrStatement> stats = new ArrayList<>();
        List<IrVariableDeclaration> vars = new ArrayList<>();
        
        for (int i = 0; i < ctx.statement().size(); i++) {
            stats.add((IrStatement) stack.pop());
        }
        
        for (int i = 0; i < ctx.var_decl().size(); i++) {
            for (int j = 0; j < ctx.var_decl(i).ID().size(); j++) {
                vars.add((IrVariableDeclaration) stack.pop());
            }
        }
                   
        Collections.reverse(stats);
        Collections.reverse(vars);
        IrBlock block;
        if (vars.isEmpty()) {
            block = new IrBlock(stats);
        } else {
            block = new IrBlock(vars, stats);
        }
        block.setLineNum(ctx.getStart().getLine());
        block.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(block);        
    }
    
    @Override
    public void exitStatement(GrammarParser.StatementContext ctx) {
        if (ctx.assign_op() != null) {
            // Assignment
            exitAssignment(ctx);
            
        } else if (ctx.TK_CONTINUE() != null) {
            // Continue
            IrContinueStatement stat = new IrContinueStatement();
            stat.setLineNum(ctx.getStart().getLine());
            stat.setColNum(ctx.getStart().getCharPositionInLine());
            stack.push(stat);
            
        } else if (ctx.TK_BREAK() != null) {
            // Break
            IrBreakStatement stat = new IrBreakStatement();
            stat.setLineNum(ctx.getStart().getLine());
            stat.setColNum(ctx.getStart().getCharPositionInLine());
            stack.push(stat);
            
        } else if (ctx.TK_FOR() != null) {
            // For loop
            exitForLoop(ctx);
        
        } else if (ctx.TK_IF() != null) {
            // If then (else)
            exitIfStatement(ctx);
        
        } else if (ctx.TK_RETURN() != null) {
            // Return
            exitReturnStatement(ctx);
        
        } else if (ctx.method_call() != null) {
            // Method call
            IrCallExpression callExpr = (IrCallExpression) stack.pop();
            IrInvokeStatement invStat = new IrInvokeStatement(callExpr);
            invStat.setLineNum(ctx.getStart().getLine());
            invStat.setColNum(ctx.getStart().getCharPositionInLine());
            stack.push(invStat);
        } else {
            throw new RuntimeException("cannot identify method type");
        }      
    }
    
    private void exitReturnStatement(GrammarParser.StatementContext ctx) {
        IrReturnStatement retStat;
        
        if (ctx.expr().size() == 1) {
            retStat = new IrReturnStatement((IrExpression) stack.pop());
        } else {
            retStat = new IrReturnStatement();
        }
        
        retStat.setLineNum(ctx.getStart().getLine());
        retStat.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(retStat);
    }
    
    private void exitIfStatement(GrammarParser.StatementContext ctx) {
        IrBlock thenBlock;
        IrBlock elseBlock;
        IrExpression ifCondition;
        
        if (ctx.TK_ELSE() != null) {
            elseBlock = (IrBlock) stack.pop();
        } else {
            elseBlock = null;
        }
        
        thenBlock = (IrBlock) stack.pop();
        ifCondition = (IrExpression) stack.pop();
        
        IrIfStatement ifStatement;
        if (ctx.TK_ELSE() != null) {
            ifStatement = new IrIfStatement(ifCondition, thenBlock, elseBlock);
        } else {
            ifStatement = new IrIfStatement(ifCondition, thenBlock);
        }        
        
        ifStatement.setLineNum(ctx.getStart().getLine());
        ifStatement.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(ifStatement);
    }
    
    private void exitForLoop(GrammarParser.StatementContext ctx) {
        IrBlock loopBlock = (IrBlock) stack.pop();
        IrExpression loopExpr = (IrExpression) stack.pop();
        IrIdentifier loopVar = (IrIdentifier) stack.pop();
        
        IrForStatement forLoop = new IrForStatement(loopVar, loopExpr, loopBlock);
        forLoop.setLineNum(ctx.getStart().getLine());
        forLoop.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(forLoop);
    }
    
    private void exitAssignment(GrammarParser.StatementContext ctx) {
        IrExpression expr = (IrExpression) stack.pop();
        IrIdentifier location = (IrIdentifier) stack.pop();
        IrAssignment.IrAssignmentOp assignOp;
        GrammarParser.Assign_opContext subCtx = ctx.assign_op();
      
        if (subCtx.ASSIGN() != null) {
            assignOp = IrAssignment.IrAssignmentOp.ASSIGN;
        } else if (subCtx.INC() != null) {
            assignOp = IrAssignment.IrAssignmentOp.INC;
        } else if (subCtx.DEC() != null) {
            assignOp = IrAssignment.IrAssignmentOp.DEC;
        } else {
            throw new RuntimeException("cannot identify assignment type");
        }        
      
        IrAssignment assign = new IrAssignment(location, assignOp, expr);
        assign.setLineNum(ctx.getStart().getLine());
        assign.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(assign);
    }
    
    @Override
    public void exitMethod_call(GrammarParser.Method_callContext ctx) {
        IrExpression callout;
        String calloutName;
        List<String> stringArgs = new ArrayList<String>();
        List<IrExpression> exprArgs = new ArrayList<IrExpression>();
                        
        if (ctx.TK_CALLOUT() == null) {
            // Method call
            calloutName = ctx.method_name().getText();
            for (int i = 0; i < ctx.expr().size(); i++) {
                exprArgs.add((IrExpression) stack.pop());
            }
            Collections.reverse(exprArgs);
            callout = new IrMethodCallExpression(calloutName, exprArgs); 
            
        } else {
            // Callout
            calloutName = ctx.STRING().getText();
            for (Callout_argContext arg : ctx.callout_arg()) {
                if (arg.STRING() != null) {
                    stringArgs.add(arg.getText());
                } else {
                    exprArgs.add((IrExpression) stack.pop());
                }
            }
            Collections.reverse(exprArgs);
            callout = new IrCalloutExpression(calloutName, exprArgs, stringArgs);
        }
        
        callout.setLineNum(ctx.getStart().getLine());
        callout.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(callout);
            
    }
    
    @Override
    public void exitLiteral(GrammarParser.LiteralContext ctx) {
        IrLiteral value;
        
        if (ctx.BOOL_LITERAL() != null) {
            value = new IrBooleanLiteral(ctx.BOOL_LITERAL().getText());
        } else if (ctx.CHAR() != null) {
            value = new IrCharLiteral(ctx.CHAR().getText());  
        } else if (ctx.INT_LITERAL() != null) { 
            value = new IrIntLiteral(ctx.INT_LITERAL().getText());
        } else {
            throw new RuntimeException("cannot identify literal");
        }
        
        value.setLineNum(ctx.getStart().getLine());
        value.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(value);
    }
    
    @Override
    public void exitLocation(GrammarParser.LocationContext ctx) {
        String varName;
        IrIdentifier location;
        
        varName = ctx.ID().getText();
        if (ctx.LBRACKET() == null) {
            location = new IrIdentifier(varName);
        } else {
            location = new IrIdentifier(varName, (IrExpression) stack.pop());
        }
        
        location.setLineNum(ctx.getStart().getLine());
        location.setColNum(ctx.getStart().getCharPositionInLine());        
        stack.push(location);
    }
    
    @Override
    public void exitNot_exp(GrammarParser.Not_expContext ctx) {
        IrExpression exp, unExp;
        
        if (ctx.NOT() == null) {
            return;
        }
        
        exp = (IrExpression) stack.pop();
        unExp = new IrUnaryExpression(IrUnaryExpression.UnaryOperator.NOT, exp);
        unExp.setLineNum(ctx.getStart().getLine());
        unExp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(unExp);
    }
    
    @Override
    public void exitMinus_exp(GrammarParser.Minus_expContext ctx) {
        IrExpression exp, unExp;
        
        if (ctx.MINUS() == null) {
            return;
        }
        
        exp = (IrExpression) stack.pop();
        unExp = new IrUnaryExpression(IrUnaryExpression.UnaryOperator.MINUS, exp);
        unExp.setLineNum(ctx.getStart().getLine());
        unExp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(unExp);
    }
    
    @Override
    public void exitAdd_exp(GrammarParser.Add_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.PLUS() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.PLUS, lhs, rhs);
        } else if (ctx.MINUS() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.MINUS, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitMult_exp(GrammarParser.Mult_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.TIMES() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.TIMES, lhs, rhs);
        } else if (ctx.OVER() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.DIVIDE, lhs, rhs);
        } else if (ctx.MOD() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.MOD, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitRel_exp(GrammarParser.Rel_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.LT() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.LT, lhs, rhs);
        } else if (ctx.LE() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.LE, lhs, rhs);
        } else if (ctx.GT() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.GT, lhs, rhs);
        } else if (ctx.GE() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.GE, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitEq_exp(GrammarParser.Eq_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.EQ() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.EQ, lhs, rhs);
        } else if (ctx.NEQ() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.NEQ, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitOr_exp(GrammarParser.Or_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.OR() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.OR, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitAnd_exp(GrammarParser.And_expContext ctx) {
        IrExpression lhs, rhs, exp;
        
        if (ctx.AND() != null) {
            rhs = (IrExpression) stack.pop();
            lhs = (IrExpression) stack.pop();
            exp = new IrBinaryExpression(IrBinaryExpression.BinaryOperator.AND, lhs, rhs);
        } else {
            return;
        }
        
        exp.setLineNum(ctx.getStart().getLine());
        exp.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(exp);
    }
    
    @Override
    public void exitProgram(GrammarParser.ProgramContext ctx) {        
        List<IrFieldDeclaration> fieldDeclarations = new ArrayList<IrFieldDeclaration>();
        List<IrMethodDeclaration> methodDeclarations = new ArrayList<IrMethodDeclaration>();
        IrClassDeclaration classVar;
        String className = "Program";
        
        for (int i = 0; i < ctx.method_decl().size(); i++) {
            methodDeclarations.add((IrMethodDeclaration) stack.pop());
        }
        
        for (int i = 0; i < ctx.field_decl().size(); i++) {
            for (int j = 0; j < ctx.field_decl(i).field().size(); j++) {
                fieldDeclarations.add((IrFieldDeclaration) stack.pop());
            }
        }
        
        Collections.reverse(fieldDeclarations);
        Collections.reverse(methodDeclarations);
        
        classVar = new IrClassDeclaration(className, fieldDeclarations, methodDeclarations);
        classVar.setLineNum(ctx.getStart().getLine());
        classVar.setColNum(ctx.getStart().getCharPositionInLine());
        stack.push(classVar);
        
    }   
    
}
