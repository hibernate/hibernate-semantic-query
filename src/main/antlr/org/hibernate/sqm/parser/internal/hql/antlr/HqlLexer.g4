lexer grammar HqlLexer;


@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.parser.internal.hql.antlr;
}

tokens {
	COLLATE,
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
	ANY,
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
	FULL,
	GROUP_BY,
	HAVING,
	HOUR,
	INDEX,
	INDICES,
	INNER,
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
	OUTER,
	POSITION,
	PROPERTIES,
	RIGHT,
	SECOND,
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
	UPPER,
	VERSIONED,
	WHEN,
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

SELECT 		: [sS] [eE] [lL] [eE] [cC] [tT];
UPDATE 		: [uU] [pP] [dD] [aA] [tT] [eE];
DELETE 		: [dD] [eE] [lL] [eE] [tT] [eE];
INSERT 		: [iI] [nN] [sS] [eE] [rR] [tT];
FROM 		: [fF] [rR] [oO] [mM];
WHERE 		: [wW] [hH] [eE] [rR] [eE];
ORDER 		: [oO] [rR] [dD] [eE] [rR];
BY 			: [bB] [yY];
IN 			: [iI] [nN];
JOIN 		: [jJ] [oO] [iI] [nN];
CROSS 		: [cC] [rR] [oO] [sS] [sS];
INNER 		: [iI] [nN] [nN] [eE] [rR];
LEFT 		: [lL] [eE] [fF] [tT];
RIGHT 		: [rR] [iI] [gG] [hH] [tT];
FULL 		: [fF] [uU] [lL] [lL];
OUTER 		: [oO] [uU] [tT] [eE] [rR];
ON			: [oO] [nN];
WITH		: [wW] [iI] [tT] [hH];
NEW			: [nN] [eE] [wW];
AND 		: [aA] [nN] [dD];
OR			: [oO] [rR];
AS			: [aA] [sS];
VALUE       : [vV] [aA] [lL] [uU] [eE];
ENTRY       : [eE] [nN] [tT] [rR] [yY];
ABS         : [aA] [bB] [sS];
ALL         : [aA] [lL] [lL];
AVG         : [aA] [vV] [gG];
ASC         : [aA] [sS] [cC];
DESC        : [dD] [eE] [sS] [cC];
BETWEEN     : [bB] [eE] [tT] [wW] [eE] [eE] [nN];
BIT_LENGTH  : [bB] [iI] [tT] [_] [lL] [eE] [nN] [gG] [tT] [hH];
BOTH        : [bB] [oO] [tT] [hH];
CAST        : [cC] [aA] [sS] [tT];

IDENTIFIER
	:	('a'..'z'|'A'..'Z'|'_'|'$'|'\u0080'..'\ufffe')('a'..'z'|'A'..'Z'|'_'|'$'|'0'..'9'|'\u0080'..'\ufffe')*
	;

QUOTED_IDENTIFIER
	: '`' ( ESCAPE_SEQUENCE | ~('\\'|'`') )* '`'
	;
