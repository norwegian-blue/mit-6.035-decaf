package ir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.tree.ParseTree;

import ir.Expression.*;
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
        IrLocation location;
        
        varName = ctx.ID().getText();
        if (ctx.LBRACKET() == null) {
            location = new IrLocation(varName);
        } else {
            location = new IrLocation(varName, (IrExpression) stack.pop());
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
        List<Field_declContext> fieldDeclaration_ctx = ctx.field_decl();
        List<Method_declContext> methodDeclaration_ctx = ctx.method_decl();
        
        List<IrFieldDeclaration> fieldDeclarations = new ArrayList<IrFieldDeclaration>();
        List<IrMethodDeclaration> methodDeclarations = new ArrayList<IrMethodDeclaration>();
        
        stack.push(new IrClassDeclaration(ctx.getTokens(1).toString(), fieldDeclarations, methodDeclarations));
    }   
    
}
