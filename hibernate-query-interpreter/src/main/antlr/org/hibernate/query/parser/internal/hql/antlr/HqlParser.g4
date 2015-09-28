parser grammar HqlParser;

options {
	tokenVocab=HqlLexer;
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.antlr;
}

@members {
	/**
	 * Determine if the text of the new upcoming token LT(1), if one, matches
	 * the passed argument.  Internally calls doesUpcomingTokenMatchAny( 1, checks )
	 */
	protected boolean doesUpcomingTokenMatchAny(String... checks) {
		return doesUpcomingTokenMatchAny( 1, checks );
	}

	/**
	 * Determine if the text of the new upcoming token LT(offset), if one, matches
	 * the passed argument.
	 */
	protected boolean doesUpcomingTokenMatchAny(int offset, String... checks) {
		final Token token = retrieveUpcomingToken( offset );
		if ( token != null ) {
			if ( token.getType() == IDENTIFIER ) {
				// todo : is this really a check we want?

				final String textToValidate = token.getText();
				if ( textToValidate != null ) {
					for ( String check : checks ) {
						if ( textToValidate.equalsIgnoreCase( check ) ) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	protected Token retrieveUpcomingToken(int offset) {
		if ( null == _input ) {
			return null;
		}
		return _input.LT( offset );
	}

	protected String retrieveUpcomingTokenText(int offset) {
		Token token = retrieveUpcomingToken( offset );
		return token == null ? null : token.getText();
	}
}

statement
	: ( selectStatement | updateStatement | deleteStatement | insertStatement ) EOF
	;

selectStatement
	: querySpec orderByClause?
//	: queryExpression orderByClause?
	;

//queryExpression
//	:	querySpec ( ( unionKeyword | intersectKeyword | exceptKeyword ) allKeyword? querySpec )*
//	;

updateStatement
	: updateKeyword fromKeyword? mainEntityPersisterReference setClause whereClause
	;

setClause
	: setKeyword assignment+
	;

assignment
	: dotIdentifierSequence EQUAL expression
	;

deleteStatement
	: deleteKeyword fromKeyword? mainEntityPersisterReference whereClause
	;

insertStatement
// todo : VERSIONED
	: insertKeyword insertSpec querySpec
	;

insertSpec
	: intoSpec targetFieldsSpec
	;

intoSpec
	: intoKeyword dotIdentifierSequence
	;

targetFieldsSpec
	: LEFT_PAREN dotIdentifierSequence (COMMA dotIdentifierSequence)* RIGHT_PAREN

	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: orderByKeyword sortSpecification (COMMA sortSpecification)*
	;

sortSpecification
	: expression collationSpecification? orderingSpecification?
	;

collationSpecification
	:	collateKeyword collateName
	;

collateName
	:	dotIdentifierSequence
	;

orderingSpecification
	:	ascendingKeyword
	|	descendingKeyword
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	:	selectClause? fromClause whereClause? ( groupByClause havingClause? )?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	selectKeyword distinctKeyword? selectionList
	;

selectionList
	: selection (COMMA selection)*
	;

selection
	// I have noticed that without this predicate, Antlr will sometimes
	// interpret `select a.b from Something ...` as `from` being the
	// select-expression alias
	: selectExpression (asKeyword? {!doesUpcomingTokenMatchAny("from")}? IDENTIFIER)?
	;

selectExpression
	:	dynamicInstantiation
	|	jpaSelectObjectSyntax
	|	expression
	;

dynamicInstantiation
	:	newKeyword dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
	;

dynamicInstantiationTarget
	:	dotIdentifierSequence
	;

dotIdentifierSequence
	:	IDENTIFIER (DOT IDENTIFIER)*
	;

path
	: dotIdentifierSequence																			# SimplePath
	| treatKeyword LEFT_PAREN dotIdentifierSequence asKeyword dotIdentifierSequence RIGHT_PAREN		# TreatedPath
	| path LEFT_BRACKET expression RIGHT_BRACKET (DOT path)?										# IndexedPath
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	dynamicInstantiationArgExpression (asKeyword? IDENTIFIER)?
	;

dynamicInstantiationArgExpression
	:	expression
	|	dynamicInstantiation
	;

jpaSelectObjectSyntax
	:	objectKeyword LEFT_PAREN IDENTIFIER RIGHT_PAREN
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: fromKeyword fromElementSpace (COMMA fromElementSpace)*
	;

fromElementSpace
	:	fromElementSpaceRoot ( crossJoin | jpaCollectionJoin | qualifiedJoin )*
	;

fromElementSpaceRoot
	: mainEntityPersisterReference
	;

mainEntityPersisterReference
	: dotIdentifierSequence (asKeyword? {!doesUpcomingTokenMatchAny("where","join")}? IDENTIFIER)?
	;

crossJoin
	: crossKeyword joinKeyword mainEntityPersisterReference
	;

jpaCollectionJoin
	:	inKeyword LEFT_PAREN path RIGHT_PAREN (asKeyword? IDENTIFIER)?
	;

qualifiedJoin
	: ( innerKeyword | ((leftKeyword|rightKeyword|fullKeyword)? outerKeyword) )? joinKeyword fetchKeyword? qualifiedJoinRhs (qualifiedJoinPredicate)?
	;

qualifiedJoinRhs
	: path (asKeyword? IDENTIFIER)?
	;

qualifiedJoinPredicate
	: (onKeyword | withKeyword) predicate
	;



// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GROUP BY clause

groupByClause
	:	groupByKeyword groupingSpecification
	;

groupingSpecification
	:	groupingValue ( COMMA groupingValue )*
	;

groupingValue
	:	expression collationSpecification?
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//HAVING clause

havingClause
	:	havingKeyword predicate
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	whereKeyword predicate
	;

predicate
	: LEFT_PAREN predicate RIGHT_PAREN								# GroupedPredicate
	| predicate orKeyword predicate									# OrPredicate
	| predicate andKeyword predicate								# AndPredicate
	| notKeyword predicate											# NegatedPredicate
	| expression isKeyword (notKeyword)? NULL						# IsNullPredicate
	| expression isKeyword (notKeyword)? emptyKeyword				# IsEmptyPredicate
	| expression EQUAL expression									# EqualityPredicate
	| expression NOT_EQUAL expression								# InequalityPredicate
	| expression GREATER expression									# GreaterThanPredicate
	| expression GREATER_EQUAL expression							# GreaterThanOrEqualPredicate
	| expression LESS expression									# LessThanPredicate
	| expression LESS_EQUAL expression								# LessThanOrEqualPredicate
	| expression inKeyword inList									# InPredicate
	| expression betweenKeyword expression andKeyword expression	# BetweenPredicate
	| expression likeKeyword expression likeEscape					# LikePredicate
	| memberOfKeyword path											# MemberOfPredicate
	;

inList
	: elementsKeyword? LEFT_PAREN dotIdentifierSequence	RIGHT_PAREN	# PersistentCollectionReferenceInList
	| LEFT_PAREN expression (COMMA expression)*	RIGHT_PAREN			# ExplicitTupleInList
	| expression													# SubQueryInList
	;

likeEscape
	: escapeKeyword expression
	;

expression
	: expression DOUBLE_PIPE expression			# ConcatenationExpression
	| expression PLUS expression				# AdditionExpression
	| expression MINUS expression				# SubtractionExpression
	| expression ASTERISK expression			# MultiplicationExpression
	| expression SLASH expression				# DivisionExpression
	| expression PERCENT expression				# ModuloExpression
	| MINUS expression							# UnaryMinusExpression
	| PLUS expression							# UnaryPlusExpression
	| literal									# LiteralExpression
	| parameter									# ParameterExpression
	| function									# FunctionExpression
	| path										# PathExpression
	| LEFT_PAREN querySpec RIGHT_PAREN			# SubQueryExpression
	;

literal
// todo : date/time literals (following JDBC escape syntax)
	: STRING_LITERAL
	| CHARACTER_LITERAL
	| INTEGER_LITERAL
	| LONG_LITERAL
	| BIG_INTEGER_LITERAL
	| FLOAT_LITERAL
	| DOUBLE_LITERAL
	| BIG_DECIMAL_LITERAL
	| HEX_LITERAL
	| OCTAL_LITERAL
	| NULL
	| TRUE
	| FALSE
	| timestampLiteral
	| dateLiteral
	| timeLiteral
	;

timestampLiteral
	: TIMESTAMP_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateLiteral
	: DATE_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

timeLiteral
	: TIME_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateTimeLiteralText
	: STRING_LITERAL | CHARACTER_LITERAL
	;

parameter
	: COLON IDENTIFIER					# NamedParameter
	| QUESTION_MARK INTEGER_LITERAL		# PositionalParameter
	;

function
	: standardFunction
	| aggregateFunction
	| jpaCollectionFunction
	| hqlCollectionFunction
	| jpaNonStandardFunction
	| nonStandardFunction
	;

jpaNonStandardFunction
	: functionKeyword LEFT_PAREN nonStandardFunctionName (COMMA nonStandardFunctionArguments)? RIGHT_PAREN
	;

nonStandardFunctionName
	: dotIdentifierSequence
	;

nonStandardFunctionArguments
	: expression (COMMA expression)*
	;

nonStandardFunction
	: nonStandardFunctionName LEFT_PAREN nonStandardFunctionArguments? RIGHT_PAREN
	;

jpaCollectionFunction
	: sizeKeyword LEFT_PAREN path RIGHT_PAREN			# CollectionSizeFunction
	| indexKeyword LEFT_PAREN IDENTIFIER RIGHT_PAREN	# CollectionIndexFunction
	| keyKeyword LEFT_PAREN path RIGHT_PAREN			# MapKeyFunction
	| valueKeyword LEFT_PAREN path RIGHT_PAREN			# CollectionValueFunction
	;

hqlCollectionFunction
	: maxindexKeyword LEFT_PAREN path RIGHT_PAREN		# MaxIndexFunction
	| maxelementKeyword LEFT_PAREN path RIGHT_PAREN		# MaxElementFunction
	| minindexKeyword LEFT_PAREN path RIGHT_PAREN		# MinIndexFunction
	| minelementKeyword LEFT_PAREN path RIGHT_PAREN		# MinElementFunction
	;

aggregateFunction
	: avgFunction
	| sumFunction
	| minFunction
	| maxFunction
	| countFunction
	;

avgFunction
	: avgKeyword LEFT_PAREN distinctKeyword? expression RIGHT_PAREN
	;

sumFunction
	: sumKeyword LEFT_PAREN distinctKeyword? expression RIGHT_PAREN
	;

minFunction
	: minKeyword LEFT_PAREN distinctKeyword? expression RIGHT_PAREN
	;

maxFunction
	: maxKeyword LEFT_PAREN distinctKeyword? expression RIGHT_PAREN
	;

countFunction
	: countKeyword LEFT_PAREN distinctKeyword? (expression | ASTERISK) RIGHT_PAREN
	;

standardFunction
	:	castFunction
	|	concatFunction
	|	substringFunction
	|	trimFunction
	|	upperFunction
	|	lowerFunction
	|	lengthFunction
	|	locateFunction
	|	absFunction
	|	sqrtFunction
	|	modFunction
	|	currentDateFunction
	|	currentTimeFunction
	|	currentTimestampFunction
	|	extractFunction
	|	positionFunction
	|	charLengthFunction
	|	octetLengthFunction
	|	bitLengthFunction
	;


castFunction
	: castkeyword LEFT_PAREN expression asKeyword dataType RIGHT_PAREN
	;

dataType
	: IDENTIFIER
	;

concatFunction
	: concatKeyword LEFT_PAREN expression (COMMA expression)+ RIGHT_PAREN
	;

substringFunction
	: substringKeyword LEFT_PAREN expression COMMA substringFunctionStartArgument (COMMA substringFunctionLengthArgument)? RIGHT_PAREN
	;

substringFunctionStartArgument
	: expression
	;

substringFunctionLengthArgument
	: expression
	;

trimFunction
	: trimKeyword LEFT_PAREN trimSpecification? trimCharacter? fromKeyword? expression RIGHT_PAREN
	;

trimSpecification
	: leadingKeyword
	| trailingKeyword
	| bothKeyword
	;

trimCharacter
	: CHARACTER_LITERAL | STRING_LITERAL
	;

upperFunction
	: upperKeyword LEFT_PAREN expression RIGHT_PAREN
	;

lowerFunction
	: lowerKeyword LEFT_PAREN expression RIGHT_PAREN
	;

lengthFunction
	: lengthKeyword LEFT_PAREN expression RIGHT_PAREN
	;

locateFunction
	: locateKeyword LEFT_PAREN locateFunctionSubstrArgument COMMA locateFunctionStringArgument (COMMA locateFunctionStartArgument)? RIGHT_PAREN
	;

locateFunctionSubstrArgument
	: expression
	;

locateFunctionStringArgument
	: expression
	;

locateFunctionStartArgument
	: expression
	;

absFunction
	:	absKeyword LEFT_PAREN expression RIGHT_PAREN
	;

sqrtFunction
	:	sqrtKeyword LEFT_PAREN expression RIGHT_PAREN
	;

modFunction
	:	modKeyword LEFT_PAREN modDividendArgument COMMA modDivisorArgument RIGHT_PAREN
	;

modDividendArgument
	: expression
	;

modDivisorArgument
	: expression
	;

currentDateFunction
	: currentDateKeyword (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimeFunction
	: currentTimeKeyword (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimestampFunction
	: currentTimestampKeyword (LEFT_PAREN RIGHT_PAREN)?
	;

extractFunction
	: extractKeyword LEFT_PAREN extractField fromKeyword expression RIGHT_PAREN
	;

extractField
	: datetimeField
	| timeZoneField
	;

datetimeField
	: nonSecondDatetimeField
	| secondKeyword
	;

nonSecondDatetimeField
	: yearKeyword
	| monthKeyword
	| dayKeyword
	| hourKeyword
	| minuteKeyword
	;

timeZoneField
	: timezoneHourKeyword
	| timezoneMinuteKeyword
	;

positionFunction
	: positionKeyword LEFT_PAREN positionSubstrArgument inKeyword positionStringArgument RIGHT_PAREN
	;

positionSubstrArgument
	: expression
	;

positionStringArgument
	: expression
	;

charLengthFunction
	: charLengthKeyword LEFT_PAREN expression RIGHT_PAREN
	;

octetLengthFunction
	: octetLengthKeyword LEFT_PAREN expression RIGHT_PAREN
	;

bitLengthFunction
	: bitLengthKeyword LEFT_PAREN expression RIGHT_PAREN
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Key word rules


absKeyword
	: {doesUpcomingTokenMatchAny("abs")}? IDENTIFIER
	;

allKeyword
	: {doesUpcomingTokenMatchAny("all")}? IDENTIFIER
	;

andKeyword
	: {doesUpcomingTokenMatchAny("and")}? IDENTIFIER
	;

asKeyword
	: {doesUpcomingTokenMatchAny("as")}? IDENTIFIER
	;

avgKeyword
	: {doesUpcomingTokenMatchAny("avg")}? IDENTIFIER
	;

ascendingKeyword
	: {(doesUpcomingTokenMatchAny("ascending","asc"))}? IDENTIFIER
	;

betweenKeyword
	: {doesUpcomingTokenMatchAny("between")}? IDENTIFIER
	;

bitLengthKeyword
	: {doesUpcomingTokenMatchAny("bit_length")}? IDENTIFIER
	;

bothKeyword
	: {doesUpcomingTokenMatchAny("both")}? IDENTIFIER
	;

castkeyword
	: {doesUpcomingTokenMatchAny("cast")}? IDENTIFIER
	;

charLengthKeyword
	: {doesUpcomingTokenMatchAny("character_length","char_length")}? IDENTIFIER
	;

classKeyword
	: {doesUpcomingTokenMatchAny("class")}? IDENTIFIER
	;

collateKeyword
	: {doesUpcomingTokenMatchAny("collate")}? IDENTIFIER
	;

concatKeyword
	: {doesUpcomingTokenMatchAny("concat")}? IDENTIFIER
	;

countKeyword
	: {doesUpcomingTokenMatchAny("count")}? IDENTIFIER
	;

crossKeyword
	: {doesUpcomingTokenMatchAny("cross")}? IDENTIFIER
	;

currentDateKeyword
	: {doesUpcomingTokenMatchAny("current_date")}? IDENTIFIER
	;

currentTimeKeyword
	: {doesUpcomingTokenMatchAny("current_time")}? IDENTIFIER
	;

currentTimestampKeyword
	: {doesUpcomingTokenMatchAny("current_timestamp")}? IDENTIFIER
	;

dayKeyword
	: {doesUpcomingTokenMatchAny("day")}? IDENTIFIER
	;

deleteKeyword
	: {doesUpcomingTokenMatchAny("delete")}? IDENTIFIER
	;

descendingKeyword
	: {(doesUpcomingTokenMatchAny("descending","desc"))}? IDENTIFIER
	;

distinctKeyword
	: {doesUpcomingTokenMatchAny("distinct")}? IDENTIFIER
	;

elementsKeyword
	: {doesUpcomingTokenMatchAny("elements")}? IDENTIFIER
	;

emptyKeyword
	: {doesUpcomingTokenMatchAny("escape")}? IDENTIFIER
	;

escapeKeyword
	: {doesUpcomingTokenMatchAny("escape")}? IDENTIFIER
	;

exceptKeyword
	: {doesUpcomingTokenMatchAny("except")}? IDENTIFIER
	;

extractKeyword
	: {doesUpcomingTokenMatchAny("extract")}? IDENTIFIER
	;

fetchKeyword
	: {doesUpcomingTokenMatchAny("fetch")}? IDENTIFIER
	;

fromKeyword
	: {doesUpcomingTokenMatchAny("from")}? IDENTIFIER
	;

fullKeyword
	: {doesUpcomingTokenMatchAny("full")}? IDENTIFIER
	;

functionKeyword
	: {doesUpcomingTokenMatchAny("function")}? IDENTIFIER
	;

groupByKeyword
	: {doesUpcomingTokenMatchAny(1,"group") && doesUpcomingTokenMatchAny(2,"by")}? IDENTIFIER IDENTIFIER
	;

havingKeyword
	: {doesUpcomingTokenMatchAny("having")}? IDENTIFIER
	;

hourKeyword
	: {doesUpcomingTokenMatchAny("hour")}? IDENTIFIER
	;

inKeyword
	: {doesUpcomingTokenMatchAny("in")}? IDENTIFIER
	;

intoKeyword
	: {doesUpcomingTokenMatchAny("into")}? IDENTIFIER
	;

indexKeyword
	: {doesUpcomingTokenMatchAny("index")}? IDENTIFIER
	;

innerKeyword
	: {doesUpcomingTokenMatchAny("inner")}? IDENTIFIER
	;

insertKeyword
	: {doesUpcomingTokenMatchAny("insert")}? IDENTIFIER
	;

isKeyword
	: {doesUpcomingTokenMatchAny("is")}? IDENTIFIER
	;

intersectKeyword
	: {doesUpcomingTokenMatchAny("intersect")}? IDENTIFIER
	;

joinKeyword
	: {doesUpcomingTokenMatchAny("join")}? IDENTIFIER
	;

keyKeyword
	: {doesUpcomingTokenMatchAny("key")}? IDENTIFIER
	;

leadingKeyword
	: {doesUpcomingTokenMatchAny("leading")}?  IDENTIFIER
	;

leftKeyword
	: {doesUpcomingTokenMatchAny("left")}?  IDENTIFIER
	;

lengthKeyword
	: {doesUpcomingTokenMatchAny("length")}?  IDENTIFIER
	;

likeKeyword
	: {doesUpcomingTokenMatchAny("like")}?  IDENTIFIER
	;

locateKeyword
	: {doesUpcomingTokenMatchAny("locate")}?  IDENTIFIER
	;

lowerKeyword
	: {doesUpcomingTokenMatchAny("lower")}?  IDENTIFIER
	;

maxKeyword
	: {doesUpcomingTokenMatchAny("max")}?  IDENTIFIER
	;

maxelementKeyword
	: {doesUpcomingTokenMatchAny("maxelement")}?  IDENTIFIER
	;

maxindexKeyword
	: {doesUpcomingTokenMatchAny("maxindex")}?  IDENTIFIER
	;

memberOfKeyword
	: {doesUpcomingTokenMatchAny(1,"member") && doesUpcomingTokenMatchAny(2,"of")}?  IDENTIFIER IDENTIFIER
	;

minKeyword
	: {doesUpcomingTokenMatchAny("min")}?  IDENTIFIER
	;

minelementKeyword
	: {doesUpcomingTokenMatchAny("minelement")}?  IDENTIFIER
	;

minindexKeyword
	: {doesUpcomingTokenMatchAny("minindex")}?  IDENTIFIER
	;

minuteKeyword
	: {doesUpcomingTokenMatchAny("minute")}?  IDENTIFIER
	;

modKeyword
	: {doesUpcomingTokenMatchAny("mod")}?  IDENTIFIER
	;

monthKeyword
	: {doesUpcomingTokenMatchAny("month")}?  IDENTIFIER
	;

newKeyword
	: {doesUpcomingTokenMatchAny("new")}?  IDENTIFIER
	;

notKeyword
	: {doesUpcomingTokenMatchAny("not")}?  IDENTIFIER
	;

objectKeyword
	: {doesUpcomingTokenMatchAny("object")}?  IDENTIFIER
	;

octetLengthKeyword
	: {doesUpcomingTokenMatchAny("octet_length")}?  IDENTIFIER
	;

onKeyword
	: {doesUpcomingTokenMatchAny("on")}?  IDENTIFIER
	;

orKeyword
	: {doesUpcomingTokenMatchAny("or")}?  IDENTIFIER
	;

orderByKeyword
	: {(doesUpcomingTokenMatchAny("order") && doesUpcomingTokenMatchAny(2, "by"))}?  IDENTIFIER IDENTIFIER
	;

outerKeyword
	: {doesUpcomingTokenMatchAny("outer")}?  IDENTIFIER
	;

positionKeyword
	: {doesUpcomingTokenMatchAny("position")}?  IDENTIFIER
	;

propertiesKeyword
	: {doesUpcomingTokenMatchAny("properties")}?  IDENTIFIER
	;

rightKeyword
	: {doesUpcomingTokenMatchAny("right")}?  IDENTIFIER
	;

secondKeyword
	: {doesUpcomingTokenMatchAny("second")}?  IDENTIFIER
	;

selectKeyword
	: {doesUpcomingTokenMatchAny("select")}?  IDENTIFIER
	;

setKeyword
	: {doesUpcomingTokenMatchAny("set")}?  IDENTIFIER
	;

sizeKeyword
	: {doesUpcomingTokenMatchAny("size")}?  IDENTIFIER
	;

sqrtKeyword
	: {doesUpcomingTokenMatchAny("sqrt")}?  IDENTIFIER
	;

substringKeyword
	: {doesUpcomingTokenMatchAny("substring")}?  IDENTIFIER
	;

sumKeyword
	: {doesUpcomingTokenMatchAny("sum")}?  IDENTIFIER
	;

timezoneHourKeyword
	: {doesUpcomingTokenMatchAny("timezone_hour")}?  IDENTIFIER
	;

timezoneMinuteKeyword
	: {doesUpcomingTokenMatchAny("timezone_minute")}?  IDENTIFIER
	;

trailingKeyword
	: {doesUpcomingTokenMatchAny("trailing")}?  IDENTIFIER
	;

treatKeyword
	: {doesUpcomingTokenMatchAny("treat")}?  IDENTIFIER
	;

trimKeyword
	: {doesUpcomingTokenMatchAny("trim")}?  IDENTIFIER
	;

unionKeyword
	: {doesUpcomingTokenMatchAny("union")}?  IDENTIFIER
	;

updateKeyword
	: {doesUpcomingTokenMatchAny("update")}?  IDENTIFIER
	;

upperKeyword
	: {doesUpcomingTokenMatchAny("upper")}?  IDENTIFIER
	;

valueKeyword
	: {doesUpcomingTokenMatchAny("value")}?  IDENTIFIER
	;

whereKeyword
	: {doesUpcomingTokenMatchAny("where")}?  IDENTIFIER
	;

withKeyword
	: {doesUpcomingTokenMatchAny("with")}?  IDENTIFIER
	;

yearKeyword
	: {doesUpcomingTokenMatchAny("year")}?  IDENTIFIER
	;
