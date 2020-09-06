grammar Grammar ;

@header {
package decaf;
}

// Keywords
KEYWORDS: 'boolean'|'break'|'callout'|'class'|'continue'|'else'|'for'|'if'|'int'|'return'|'void' ; 

// Integers
INT_LITERAL : DEC_LITERAL | HEX_LITERAL ;

fragment DEC_LITERAL : '-'? DIGIT+ ;
fragment HEX_LITERAL : '0x' HEX_DIGIT+ ;
fragment HEX_DIGIT : DIGIT | [a-fA-F] ;
fragment DIGIT : [0-9] ;

// Booleans
BOOL_LITERAL : 'true' | 'false' ;

// Chars
CHAR : '\'' CHARACTER '\'' ;
fragment CHARACTER : (ESC|' '|'!'|('#'..'&')|'('..'['|']'..'~') ;
fragment ESC : '\\' ('n'|'"'|'t'|'\\'|'\'') ;

// Strings
STRING : '"' CHARACTER* '"' ;

// Identifier
ID : [a-z]+ ;

SL_COMMENT : '//' ~[\n]* '\n' -> skip ;
WS : [ \t\n]+ -> skip ;

program : INT_LITERAL ;
