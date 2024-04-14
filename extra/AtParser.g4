parser grammar AtParser;

options { tokenVocab=AtLexer; }

file : (line CRLF)* line? EOF ;

line : entry? WS? COMMENT? ;

entry : keyword WS class_name (WS line_value)? ;
line_value : function | field_name | wildcard_method | wildcard_field ;

wildcard_method : ASTERISK_METHOD ;
wildcard_field : ASTERISK ;
keyword : KEYWORD ;
class_name : CLASS_NAME ;
field_name : NAME_ELEMENT ;

function : func_name OPEN_PARAM argument* CLOSE_PARAM return_value ;
func_name : NAME_ELEMENT ;

argument : PRIMITIVE | class_value ;
return_value : PRIMITIVE | class_value ;
class_value : CLASS_VALUE ;
