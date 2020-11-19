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
import cfg.CfgProgram;

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
                
        	} else if (CLI.target == CLI.INTER || CLI.target == CLI.ASSEMBLY) {
        	    
                //****************************************************** 
                // INTERPRETER 
                //******************************************************     
        	    
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
                IrClassDeclaration program = loader.getAbstractSyntaxTree();
                
                // Simplify tree
                TreeSimplifier simplify = new TreeSimplifier();
                program.accept(simplify);
                
                if (CLI.debug && CLI.target == CLI.INTER) {
                    System.out.printf(program.toString());
                    System.out.println();
                }
                
                // Run semantic check
                SemanticChecker check = new SemanticChecker();
                if (!program.accept(check)) {
                    System.err.println(check.toString());
                    throw new Error("Semantic check failed");
                }
                                             
                if (CLI.debug && CLI.target == CLI.INTER) {
                    System.out.println("### Semantic check passed ###");
                }
                
                if (CLI.target == CLI.INTER) {
                    System.exit(0);
                }
                
                
                //****************************************************** 
                // ASSEMBLER  
                //******************************************************
                
                // Rename to unique identifiers
                program.accept(new IrRenamer());    
                assert(program.accept(check));
                if (CLI.debug) {
                    System.out.println("################# Variable renaming (no duplicate names) #################");
                    System.out.print(program.toString() + "\n");
                }

                // Create Control Flow Graph
                CfgProgram controlFlow = new CfgProgram(program);
                if (CLI.debug) {
                    System.out.println("################# Control Flow Graph #################");
                    System.out.println(controlFlow.toString());
                }
                    
                // TODO: controlFlow.flatten();
                // TODO: controlFlow.blockify();
                
                // Assemble
                // TODO: assembler
                System.out.println("Done");
        	}
        	
        } catch(Exception e) {
        	// print the error:
            System.out.println(CLI.infile+" "+e);
            System.exit(1);
        }
    }
}

