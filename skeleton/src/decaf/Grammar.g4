grammar Grammar ;

@header {
package decaf;
}

/////////////////////////////////
// Lexer rules
/////////////////////////////////

// Keywords
TK_BOOL : 'boolean' ;
TK_BREAK : 'break' ;
TK_CALLOUT : 'callout' ;
TK_CLASS : 'class' ;
TK_CONTINUE : 'continue' ;
TK_ELSE : 'else' ;
TK_FALSE : 'false' ;
TK_FOR : 'for' ;
TK_IF : 'if' ;
TK_INT : 'int' ;
TK_TRUE : 'true' ;
TK_RETURN : 'return' ;
TK_VOID : 'void' ;

// Integers
INT_LITERAL : DEC_LITERAL | HEX_LITERAL ;

fragment DEC_LITERAL : DIGIT+ ;
fragment HEX_LITERAL : '0x' HEX_DIGIT+ ;
fragment HEX_DIGIT : DIGIT | [a-fA-F] ;
fragment DIGIT : [0-9] ;

// Booleans
BOOL_LITERAL : TK_TRUE | TK_FALSE ; 

// Chars
CHAR : '\'' CHARACTER '\'' ;
fragment CHARACTER : (ESC|' '|'!'|('#'..'&')|'('..'['|']'..'~') ;
fragment ESC : '\\' ('n'|'"'|'t'|'\\'|'\'') ;

// Strings
STRING : '"' CHARACTER* '"' ;

// Identifier
ID : ALPHA ALPHA_NUM* ;
fragment ALPHA : [a-zA-Z_] ;
fragment ALPHA_NUM : ALPHA | DIGIT ;

// Operators, assignments, and symbols 
INC : '+=' ;
DEC : '-=' ;
PLUS : '+' ;
MINUS : '-' ;
TIMES : '*' ;
OVER : '/' ;
MOD : '%' ;
EQ : '==' ;
NEQ : '!=' ;
ASSIGN : '=' ;
AND : '&&' ;
OR : '||' ;
GE : '>=' ;
GT : '>' ;
LE : '<=' ;
LT : '<' ;
LCURLY : '{' ;
RCURLY : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
LPAR : '(' ;
RPAR : ')' ;
COMMA : ',' ;
SEMI : ';' ;
NOT : '!' ;

// Comments and whitespace
SL_COMMENT : '//' ~[\n]* '\n' -> skip ;
WS : [ \t\n]+ -> skip ;


/////////////////////////////////
// Syntax rules
/////////////////////////////////

program : TK_CLASS 'Program' LCURLY field_decl* method_decl* RCURLY  ;

field_decl : type field (COMMA field)* SEMI ;
field : ID (LBRACKET INT_LITERAL RBRACKET)? ;

method_decl : (type | TK_VOID) ID LPAR method_par RPAR block;
method_par : ((type ID) (COMMA type ID)*)? ;

type : TK_INT | TK_BOOL ;

block : LCURLY var_decl* statement* RCURLY ;

var_decl : (type ID) (COMMA type ID)* SEMI ;

statement : location assign_op expr SEMI
	  | method_call SEMI
	  | TK_IF LPAR expr RPAR block (TK_ELSE block)?
	  | TK_FOR ID EQ expr COMMA expr block
	  | TK_RETURN expr? SEMI
	  | TK_CONTINUE SEMI
	  | block ;

assign_op : (ASSIGN | INC | DEC) ;

method_call : method_name LPAR (expr (COMMA expr)*)? RPAR 
	    | TK_CALLOUT LPAR STRING callout_arg* RPAR ;

method_name : ID ;

location : ID (LBRACKET expr RBRACKET)? ;

expr : location
     | method_call
     | or_exp ;

callout_arg : expr | STRING ;

or_exp : or_exp OR and_exp
       | and_exp ;

and_exp : and_exp AND eq_exp
	| eq_exp ;

eq_exp : eq_exp (EQ | NEQ) rel_exp
       | rel_exp ;

rel_exp	 : rel_exp (LT | LE | GT | GE) add_exp
	 | add_exp ;

add_exp : add_exp (PLUS | MINUS) mult_exp
	| mult_exp ;

mult_exp : mult_exp (TIMES | OVER | MOD) minus_exp
         | minus_exp ;

minus_exp : MINUS minus_exp
	  | not_exp ;

not_exp : NOT not_exp
	| atom ;

atom : literal | (LPAR expr RPAR) ;

literal : INT_LITERAL | CHAR | BOOL_LITERAL ;
