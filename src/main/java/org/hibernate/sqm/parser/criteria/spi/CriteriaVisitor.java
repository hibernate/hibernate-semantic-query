/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi;

import java.util.List;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Subquery;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.parser.criteria.spi.expression.BooleanExpressionCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.expression.LiteralCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.ParameterCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.CastFunctionCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.expression.function.GenericFunctionCriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.path.RootImplementor;
import org.hibernate.sqm.parser.criteria.spi.predicate.ComparisonCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.NegatedCriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.NullnessCriteriaPredicate;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralSqmExpression;
import org.hibernate.sqm.query.expression.ParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

/**
 * @author Steve Ebersole
 */
public interface CriteriaVisitor {

	<T extends Enum> ConstantEnumSqmExpression<T> visitEnumConstant(T value);
	<T> ConstantFieldSqmExpression<T> visitConstant(T value);
	<T> ConstantFieldSqmExpression<T> visitConstant(T value, BasicType typeDescriptor);

	UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression);

	UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression,
			BasicType resultType);

	BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2);

	BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType);

	SqmFrom visitIdentificationVariableReference(From reference);
	AttributeBinding visitAttributeReference(From attributeSource, String attributeName);

	GenericFunctionSqmExpression visitFunction(String name, BasicType resultTypeDescriptor, List<javax.persistence.criteria.Expression<?>> expressions);
	GenericFunctionSqmExpression visitFunction(String name, BasicType resultTypeDescriptor, javax.persistence.criteria.Expression<?>... expressions);

	AvgFunctionSqmExpression visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	AvgFunctionSqmExpression visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	CountFunctionSqmExpression visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	CountFunctionSqmExpression visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct);
	CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct, BasicType resultType);

	MaxFunctionSqmExpression visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	MaxFunctionSqmExpression visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	MinFunctionSqmExpression visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	MinFunctionSqmExpression visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	SumFunctionSqmExpression visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	SumFunctionSqmExpression visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	ConcatSqmExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2);

	ConcatSqmExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType);

	EntityTypeSqmExpression visitEntityType(String identificationVariable);
	EntityTypeSqmExpression visitEntityType(String identificationVariable, String attributeName);

//	CollectionSizeFunction visitCollectionSizeFunction();
//
//	CollectionValueFunction visitCollectionValueBinding();
//	MapKeyFunction visitMapKeyBinding();
//	MapEntryFunction visitMapEntryFunction();

	SubQuerySqmExpression visitSubQuery(Subquery subquery);

	AndSqmPredicate visitAndPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates);
	OrSqmPredicate visitOrPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates);

	EmptinessSqmPredicate visitEmptinessPredicate(From attributeSource, String attributeName, boolean negated);
	MemberOfSqmPredicate visitMemberOfPredicate(From attributeSource, String attributeName, boolean negated);

	BetweenSqmPredicate visitBetweenPredicate(
			javax.persistence.criteria.Expression expression,
			javax.persistence.criteria.Expression lowerBound,
			javax.persistence.criteria.Expression upperBound,
			boolean negated);


	LikeSqmPredicate visitLikePredicate(
			javax.persistence.criteria.Expression<String> matchExpression,
			javax.persistence.criteria.Expression<String> pattern,
			javax.persistence.criteria.Expression<Character> escapeCharacter,
			boolean negated);

	InSubQuerySqmPredicate visitInSubQueryPredicate(
			javax.persistence.criteria.Expression testExpression,
			Subquery subquery,
			boolean negated);

	InListSqmPredicate visitInTupleListPredicate(
			javax.persistence.criteria.Expression testExpression,
			List<javax.persistence.criteria.Expression> listExpressions,
			boolean negated);

	SqmExpression visitRoot(RootImplementor root);

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// New signatures

	<T> LiteralSqmExpression<T> visitLiteral(LiteralCriteriaExpression expression);

	<T> ParameterSqmExpression visitParameter(ParameterCriteriaExpression<T> expression);

	<T,Y> CastFunctionSqmExpression visitCastFunction(CastFunctionCriteriaExpression<T,Y> function);
	<T> GenericFunctionSqmExpression visitGenericFunction(GenericFunctionCriteriaExpression<T> function);



	NegatedSqmPredicate visitNegatedPredicate(NegatedCriteriaPredicate predicate);

	BooleanExpressionSqmPredicate visitBooleanExpressionPredicate(BooleanExpressionCriteriaPredicate predicate);

	NullnessSqmPredicate visitNullnessPredicate(NullnessCriteriaPredicate predicate);

	RelationalSqmPredicate visitRelationalPredicate(ComparisonCriteriaPredicate predicate);

}
