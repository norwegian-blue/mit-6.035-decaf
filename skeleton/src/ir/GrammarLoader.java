package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
    public void exitProgram(GrammarParser.ProgramContext ctx) {
        List<Field_declContext> fieldDeclaration_ctx = ctx.field_decl();
        List<Method_declContext> methodDeclaration_ctx = ctx.method_decl();
        
        List<IrFieldDeclaration> fieldDeclarations = new ArrayList<IrFieldDeclaration>();
        List<IrMethodDeclaration> methodDeclarations = new ArrayList<IrMethodDeclaration>();
        
        stack.push(new IrClassDeclaration(ctx.getTokens(1).toString(), fieldDeclarations, methodDeclarations));
    }
    
    @Override
    public void exitField_decl(GrammarParser.Field_declContext ctx) {
    }
    
    @Override
    public void exitMethod_decl(GrammarParser.Method_declContext ctx) {
    }
    
    
}
