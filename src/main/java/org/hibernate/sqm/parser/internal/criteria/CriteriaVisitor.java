/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.criteria;

import java.util.List;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Subquery;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.FunctionExpression;
import org.hibernate.sqm.query.expression.LiteralExpression;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.ParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InListPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.select.SelectClause;

/**
 * @author Steve Ebersole
 */
public interface CriteriaVisitor {
	SelectClause visitSelectClause(AbstractQuery jpaCriteria);

	<T> LiteralExpression<T> visitLiteral(T value);
	<T> LiteralExpression<T> visitLiteral(T value, BasicType<T> typeDescriptor);

	<T extends Enum> ConstantEnumExpression<T> visitEnumConstant(T value);
	<T> ConstantFieldExpression<T> visitConstant(T value);
	<T> ConstantFieldExpression<T> visitConstant(T value, BasicType<T> typeDescriptor);

	ParameterExpression visitParameter(javax.persistence.criteria.ParameterExpression param);
	ParameterExpression visitParameter(javax.persistence.criteria.ParameterExpression param, Type typeDescriptor);

	UnaryOperationExpression visitUnaryOperation(
			UnaryOperationExpression.Operation operation,
			javax.persistence.criteria.Expression expression);

	UnaryOperationExpression visitUnaryOperation(
			UnaryOperationExpression.Operation operation,
			javax.persistence.criteria.Expression expression,
			BasicType resultType);

	BinaryArithmeticExpression visitArithmetic(
			BinaryArithmeticExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2);

	BinaryArithmeticExpression visitArithmetic(
			BinaryArithmeticExpression.Operation operation,
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType);

	FromElementBinding visitIdentificationVariableReference(From reference);
	AttributeReferenceExpression visitAttributeReference(From attributeSource, String attributeName);

	FunctionExpression visitFunction(String name, BasicType resultTypeDescriptor, List<javax.persistence.criteria.Expression> expressions);

	AvgFunction visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	AvgFunction visitAvgFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	CountFunction visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	CountFunction visitCountFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	CountStarFunction visitCountStarFunction(boolean distinct);
	CountStarFunction visitCountStarFunction(boolean distinct, BasicType resultType);

	MaxFunction visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	MaxFunction visitMaxFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	MinFunction visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	MinFunction visitMinFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	SumFunction visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct);
	SumFunction visitSumFunction(javax.persistence.criteria.Expression expression, boolean distinct, BasicType resultType);

	ConcatExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2);

	ConcatExpression visitConcat(
			javax.persistence.criteria.Expression expression1,
			javax.persistence.criteria.Expression expression2,
			BasicType resultType);

	EntityTypeExpression visitEntityType(String identificationVariable);
	EntityTypeExpression visitEntityType(String identificationVariable, String attributeName);

//	CollectionSizeFunction visitCollectionSizeFunction();
//
//	CollectionValueFunction visitCollectionValueFunction();
//	MapKeyFunction visitMapKeyFunction();
//	MapEntryFunction visitMapEntryFunction();

	SubQueryExpression visitSubQuery(Subquery subquery);

	AndPredicate visitAndPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates);
	OrPredicate visitOrPredicate(List<javax.persistence.criteria.Expression<Boolean>> predicates);

	NegatedPredicate visitPredicateNegation(javax.persistence.criteria.Expression<Boolean> expression);

	NullnessPredicate visitNullnessPredicate(javax.persistence.criteria.Expression expression, boolean negated);

	EmptinessPredicate visitEmptinessPredicate(From attributeSource, String attributeName, boolean negated);
	MemberOfPredicate visitMemberOfPredicate(From attributeSource, String attributeName, boolean negated);

	BetweenPredicate visitBetweenPredicate(
			javax.persistence.criteria.Expression expression,
			javax.persistence.criteria.Expression lowerBound,
			javax.persistence.criteria.Expression upperBound,
			boolean negated);

	RelationalPredicate visitRelationalPredicate(
			javax.persistence.criteria.Expression expression1,
			RelationalPredicate.Type type,
			javax.persistence.criteria.Expression expression2);

	LikePredicate visitLikePredicate(
			javax.persistence.criteria.Expression<String> matchExpression,
			javax.persistence.criteria.Expression<String> pattern,
			javax.persistence.criteria.Expression<Character> escapeCharacter,
			boolean negated);

	InSubQueryPredicate visitInSubQueryPredicate(
			javax.persistence.criteria.Expression testExpression,
			Subquery subquery,
			boolean negated);

	InListPredicate visitInTupleListPredicate(
			javax.persistence.criteria.Expression testExpression,
			List<javax.persistence.criteria.Expression> listExpressions,
			boolean negated);
}
