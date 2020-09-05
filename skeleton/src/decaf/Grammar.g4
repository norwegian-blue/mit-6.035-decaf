grammar Grammar ;

@header {
package decaf;
}

WS : [ \t\n]+ -> skip ;
SL_COMMENT : '//' ~[\n]* '\n' -> skip ;

INT_LITERAL : DEC_LITERAL | HEX_LITERAL ;
BOOL_LITERAL : 'true' | 'false' ;
ID : ALPHA ;
STRING : '"' ALPHA '"' ;
CHAR : '"' DIGIT '"' ;

fragment
DEC_LITERAL : '-'? DIGIT (DIGIT)* ;
HEX_LITERAL : '0x' HEX_DIGIT (HEX_DIGIT)* ;
HEX_DIGIT : DIGIT | [a-fA-F] ;
DIGIT : [0-9] ;
ALPHA : [a-zA-Z_] ;

ESC : '\\' ('n'|'"'|'t'|'\\'|'\\\\') ;

program : INT_LITERAL ;
