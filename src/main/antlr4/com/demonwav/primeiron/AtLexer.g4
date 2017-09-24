lexer grammar AtLexer;

ASTERISK : '*' | '*()' ;
OPEN_PARAM : '(' -> pushMode(ARGS) ;
COMMENT : '#' ~('\r' | '\n')* ;

KEYWORD : ('public' | 'private' | 'protected' | 'default') ([-+]'f')? ;
NAME_ELEMENT : [a-zA-Z0-9_]+ | '<init>' ;
CLASS_NAME : [a-zA-Z_$0-9.]*[a-zA-Z_$0-9] ;

WS : [ \t]+ ;
CRLF : ('\r' | '\n' | '\r\n') -> popMode ;

mode ARGS ;
PRIMITIVE : [ZBCSIFDJV] ;
CLASS_VALUE : '['+ [ZBCSIFDJ] | '['* 'L' ~[;\n]+ ';' ;
CLOSE_PARAM : ')' ;
ARGS_CRLF : ('\r' | '\n' | '\r\n') -> type(CRLF) ;
ARGS_COMMENT : COMMENT -> type(COMMENT) ;
ARGS_WS : WS -> type(WS) ;
