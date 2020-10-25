import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.CommonTokenStream;
import java6035.tools.CLI.*;
import semantic.SemanticChecker;
import semantic.TreeSimplifier;
import decaf.GrammarLexer;
import decaf.GrammarParser;
import ir.*;
import ir.Declaration.IrClassDeclaration;

class Main {
    public static void main(String[] args) {
        try {
        	CLI.parse (args, new String[0]);
        	
        	CharStream inputStream = args.length == 0 ?
                    CharStreams.fromStream(System.in) : CharStreams.fromFileName(CLI.infile);

        	if (CLI.target == CLI.SCAN) {
        	    
        		GrammarLexer lexer = new GrammarLexer(inputStream);
        		Token token;
        		boolean done = false;
        		while (!done)
        		{
        			try
        			{
		        		for (token=lexer.nextToken(); token.getType()!=GrammarParser.EOF; token=lexer.nextToken())
		        		{
		        			String type = "";
		        			String text = token.getText();
		
		        			switch (token.getType())
		        			{
		        			case GrammarParser.ID:
		        				type = " IDENTIFIER";
		        				break;
		        			case GrammarParser.BOOL_LITERAL:
		        			    type = " BOOLEANLITERAL";
		        			    break;
		        			case GrammarParser.INT_LITERAL:
		        			    type = " INTLITERAL";
		        			    break;
		        			case GrammarParser.CHAR:
		        			    type = " CHARLITERAL";
		        			    break;
		        			case GrammarParser.STRING:
		        			    type = " STRINGLITERAL";
		        			    break;
		        			}
		        			System.out.println (token.getLine() + type + " " + text);
		        		}
		        		done = true;
        			} catch(RecognitionException re) {
        	        	// print the error:
        	            System.out.println(CLI.infile+" "+re);
        	            lexer.recover (re);
        	        }
        		}
        		
        	} else if (CLI.target == CLI.PARSE || CLI.target == CLI.DEFAULT) {
        	    
        		GrammarLexer lexer = new GrammarLexer(inputStream);
        		CommonTokenStream tokens = new CommonTokenStream(lexer);
        		GrammarParser parser = new GrammarParser (tokens);
        		
        		if (CLI.debug) {
        		    parser.setTrace(true);
        		}
        		
                parser.program();
                
                if (parser.getNumberOfSyntaxErrors() > 0) {
                    throw new Error("Syntax error");
                }
                
        	} else if (CLI.target == CLI.INTER) {
        	    
        	    GrammarLexer lexer = new GrammarLexer(inputStream);
        	    CommonTokenStream tokens = new CommonTokenStream(lexer);
                GrammarParser parser = new GrammarParser (tokens);                               
                
                ParseTreeWalker walker = new ParseTreeWalker();
                GrammarLoader loader = new GrammarLoader();
                
                ParseTree tree = parser.program(); 
                
                if (parser.getNumberOfSyntaxErrors() > 0) {
                    throw new Error("Syntax error");
                }
                
                walker.walk(loader, tree);
                Ir program = loader.getAbstractSyntaxTree();
                                
                if (program.isClass()) {
                    // Simplify tree
                    TreeSimplifier simplify = new TreeSimplifier();
                    ((IrClassDeclaration)program).accept(simplify);
                    
                    if (CLI.debug) {
                        System.out.printf(program.toString());
                        System.out.println();
                    }
                    
                    // Run semantic check
                    SemanticChecker check = new SemanticChecker();
                    if (!((IrClassDeclaration)program).accept(check)) {
                        check.printErrors();
                        throw new Error("Semantic check failed");
                    }
                } else {
                    throw new Error("Could not get a valid AST for the program");
                }
                
                if (CLI.debug) {
                    System.out.println("### Semantic check passed ###");
                }
        	}
        	
        } catch(Exception e) {
        	// print the error:
            System.out.println(CLI.infile+" "+e);
            System.exit(1);
        }
    }
}

