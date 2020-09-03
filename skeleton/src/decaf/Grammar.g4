grammar Grammar ;

@header {
package decaf;
}

tokens { TK_class }

LCURLY : '{' ;
RCURLY : '}' ;

ID : [a-zA-Z]+ ;

WS : [ \t\n]+ -> skip ;

SL_COMMENT : '//' ~[\n]* '\n' -> skip ;

CHAR : '\'' (ESC|~'\'') '\'' ;
STRING : '"' (ESC|~'"')* '"' ;

fragment
ESC : '\\' ('n'|'"') ;

program : TK_class ID LCURLY RCURLY EOF ;
