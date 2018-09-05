lexer grammar AtLexer;

COMMENT : '#' ~('\r' | '\n')* ;

KEYWORD : ('public' | 'private' | 'protected' | 'default') ([-+]'f')? -> pushMode(CLASS_NAME_MODE) ;

WS : [ \t]+ ;

CRLF : '\r' | '\n' | '\r\n' ;

mode CLASS_NAME_MODE ;
CLASS_NAME : ([\p{L}_\p{Sc}][\p{L}\p{N}_\p{Sc}]*'.')*[\p{L}_\p{Sc}][\p{L}\p{N}_\p{Sc}]* -> popMode, pushMode(MEMBER_NAME) ;
CLASS_NAME_MODE_CRLF : CRLF -> popMode, type(CRLF) ;
CLASS_NAME_MODE_COMMENT : COMMENT -> type(COMMENT) ;
CLASS_NAME_MODE_WS : WS -> type(WS) ;

mode MEMBER_NAME ;
ASTERISK : '*' ;
ASTERISK_METHOD : '*()' ;
NAME_ELEMENT : (([\p{L}_\p{Sc}][\p{L}\p{N}_\p{Sc}]*) | '<init>') -> popMode, pushMode(TYPES) ;
MEMBER_NAME_CRLF : CRLF -> popMode, type(CRLF) ;
MEMBER_NAME_COMMENT : COMMENT -> type(COMMENT) ;
MEMBER_NAME_WS : WS -> type(WS) ;

mode TYPES ;
OPEN_PARAM : '(' ;
CLOSE_PARAM : ')' ;
CLASS_VALUE : '['+ [ZBCSIFDJ] | '['* 'L' ~[;\n]+ ';' ;
PRIMITIVE : [ZBCSIFDJV] ;
TYPES_CRLF : CRLF -> popMode, type(CRLF) ;
TYPES_COMMENT : COMMENT -> type(COMMENT) ;
TYPES_WS : WS -> type(WS) ;
