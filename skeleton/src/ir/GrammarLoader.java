package ir;

import java.util.Stack;
import decaf.GrammarBaseListener;
import decaf.GrammarParser;
import ir.Expression.*;


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
        return stack.get(0);
    }
    
    @Override
    public void exitProgram(GrammarParser.ProgramContext ctx) {
        System.out.println("here");
    }
    
    @Override
    public void exitField_decl(GrammarParser.Field_declContext ctx) {
        stack.push(new IrIntLiteral());
    }
    
    @Override
    public void exitMethod_decl(GrammarParser.Method_declContext ctx) {
        stack.push(new IrIntLiteral());
    }
}
