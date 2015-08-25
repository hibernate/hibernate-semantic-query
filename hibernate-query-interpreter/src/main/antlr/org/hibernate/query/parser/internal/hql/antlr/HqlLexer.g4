lexer grammar HqlLexer;


@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.antlr;
}

tokens {
//GENERIC SQL TOKENS
//	TABLE,
//	COLUMN,
//	COLUMN_LIST,

//VIRTUAL TOKENS
//	ALIAS_NAME,
//	ALIAS_REFERENCE,
//	ATTRIBUTE_REFERENCE,
	BETWEEN_LIST,
	COLLATE,
//	COLLECTION_EXPRESSION,
	DISCRIMINATOR,
	DYNAMIC_INSTANTIATION_ARG,
	DYNAMIC_INSTANTIATION,
	ENTITY_NAME,
	ENTITY_PERSISTER_REF,
	FILTER,
	FUNCTION,
//	GENERAL_FUNCTION_CALL,
//	GENERAL_FUNCTION_ARGUMENTS,
	GROUPING_VALUE,
	IN_LIST,
	INSERTABILITY_SPEC,
	IS_NOT_EMPTY,
	IS_NOT_NULL,
	IS_NULL,
	JAVA_CONSTANT,
	JPA_POSITIONAL_PARAM,
	NAMED_PARAM,
	NOT_BETWEEN,
	NOT_IN,
	NOT_LIKE,
	NOT_MEMBER_OF,
	ORDER_SPEC,
	PATH,
	PERSISTER_JOIN,
	PERSISTER_SPACE,
	POSITIONAL_PARAM,
	PROP_FETCH,
	QUALIFIED_JOIN,
	QUERY_SPEC,
	QUERY,
	SEARCHED_CASE,
	SELECT_FROM,
	SELECT_ITEM,
	SELECT_LIST,
	SIMPLE_CASE,
	SORT_SPEC,
	SUB_QUERY,
	UNARY_MINUS,
	UNARY_PLUS,
	VECTOR_EXPR,
	VERSIONED_VALUE,
	CONST_STRING_VALUE,

//SOFT KEYWORDS
	ABS,
	ALL,
	AND,
	ANY,
	AS,
	AVG,
	BETWEEN,
	BIT_LENGTH,
	BOTH,
	CAST,
	CHARACTER_LENGTH,
	CLASS,
	COALESCE,
	CONCAT,
	COUNT,
	CROSS,
	CURRENT_DATE,
	CURRENT_TIME,
	CURRENT_TIMESTAMP,
	DAY,
	DELETE,
	DISTINCT,
	ELEMENTS,
	ELSE,
	EMPTY,
	END,
	ESCAPE,
	EXCEPT,
	EXISTS,
	EXTRACT,
	FETCH,
	FROM,
	FULL,
	GROUP_BY,
	HAVING,
	HOUR,
	IN,
	INDEX,
	INDICES,
	INNER,
	INSERT,
	INTERSECT,
	INTO,
	IS_EMPTY,
	IS,
	JOIN,
	LEADING,
	LEFT,
	LENGTH,
	LIKE,
	LOCATE,
	LOWER,
	MAX,
	MAXELEMENT,
	MAXINDEX,
	MEMBER_OF,
	MIN,
	MINELEMENT,
	MININDEX,
	MINUTE,
	MOD,
	MONTH,
	NEW,
	NOT,
	NULLIF,
	OCTET_LENGTH,
	ON,
	OR,
	ORDER_BY,
	OUTER,
	POSITION,
	PROPERTIES,
	RIGHT,
	SECOND,
	SELECT,
	SET,
	SIZE,
	SOME,
	SQRT,
	SUBSTRING,
	SUM,
	THEN,
	TIMEZONE_HOUR,
	TIMEZONE_MINUTE,
	TRAILING,
	TRIM,
	UNION,
	UPDATE,
	UPPER,
	VERSIONED,
	WHEN,
	WHERE,
	WITH,
	YEAR
}

WS : ( ' ' | '\t' | '\f' | EOL ) -> skip;

fragment
EOL	: [\r\n]+;

INTEGER_LITERAL : INTEGER_NUMBER ;

fragment
INTEGER_NUMBER : ('0' | '1'..'9' '0'..'9'*) ;

LONG_LITERAL : INTEGER_NUMBER ('l'|'L');

BIG_INTEGER_LITERAL : INTEGER_NUMBER ('bi'|'BI') ;

HEX_LITERAL : '0' ('x'|'X') HEX_DIGIT+ ('l'|'L')? ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

OCTAL_LITERAL : '0' ('0'..'7')+ ('l'|'L')? ;

FLOAT_LITERAL : FLOATING_POINT_NUMBER ('f'|'F')? ;

fragment
FLOATING_POINT_NUMBER
	: ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
	| '.' ('0'..'9')+ EXPONENT?
    | ('0'..'9')+ EXPONENT
    | ('0'..'9')+
	;

DOUBLE_LITERAL : FLOATING_POINT_NUMBER ('d'|'D') ;

BIG_DECIMAL_LITERAL : FLOATING_POINT_NUMBER ('bd'|'BD') ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

CHARACTER_LITERAL
	:	'\'' ( ESCAPE_SEQUENCE | ~('\''|'\\') ) '\'' {setText(getText().substring(1, getText().length()-1));}
	;

STRING_LITERAL
	:	'"' ( ESCAPE_SEQUENCE | ~('\\'|'"') )* '"' {setText(getText().substring(1, getText().length()-1));}
	|	('\'' ( ESCAPE_SEQUENCE | ~('\\'|'\'') )* '\'')+ {setText(getText().substring(1, getText().length()-1).replace("''", "'"));}
	;

fragment
ESCAPE_SEQUENCE
	:	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
	|	UNICODE_ESCAPE
	|	OCTAL_ESCAPE
	;

fragment
OCTAL_ESCAPE
	:	'\\' ('0'..'3') ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7') ('0'..'7')
	|	'\\' ('0'..'7')
	;

fragment
UNICODE_ESCAPE
	:	'\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
	;

// ESCAPE start tokens
TIMESTAMP_ESCAPE_START : '{ts';
DATE_ESCAPE_START : '{d';
TIME_ESCAPE_START : '{t';

TRUE : 'true';
FALSE :	'false';
NULL : 'null';

EQUAL : '=';
NOT_EQUAL : '!=' | '^=' | '<>';
GREATER : '>';
GREATER_EQUAL : '>=';
LESS : '<';
LESS_EQUAL : '<=';

COMMA :	',';
DOT	: '.';
LEFT_PAREN : '(';
RIGHT_PAREN	: ')';
LEFT_BRACKET : '[';
RIGHT_BRACKET : ']';
LEFT_BRACE : '{';
RIGHT_BRACE : '}';
PLUS : '+';
MINUS :	'-';
ASTERISK : '*';
SLASH : '/';
PERCENT	: '%';
AMPERSAND : '&';
SEMICOLON :	';';
COLON : ':';
PIPE : '|';
DOUBLE_PIPE : '||';
QUESTION_MARK :	'?';
ARROW :	'->';

IDENTIFIER
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
	;

QUOTED_IDENTIFIER
	: '`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
	;

