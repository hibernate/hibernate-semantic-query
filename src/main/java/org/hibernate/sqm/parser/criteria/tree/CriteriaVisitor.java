/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree;

import java.util.List;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.parser.criteria.tree.from.JpaFrom;
import org.hibernate.sqm.parser.criteria.tree.from.JpaRoot;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.sqm.query.expression.LiteralSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.domain.SingularAttributeReference;
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
import org.hibernate.sqm.query.predicate.RelationalPredicateOperator;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;

/**
 * @author Steve Ebersole
 */
public interface CriteriaVisitor {

	<T extends Enum> ConstantEnumSqmExpression<T> visitEnumConstant(T value);

	<T> LiteralSqmExpression<T> visitConstant(T value);
	<T> LiteralSqmExpression<T> visitConstant(T value, Class<T> javaType);

	UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			JpaExpression<?> expression);

	UnaryOperationSqmExpression visitUnaryOperation(
			UnaryOperationSqmExpression.Operation operation,
			JpaExpression<?> expression,
			BasicType resultType);

	BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			JpaExpression<?> expression1,
			JpaExpression<?> expression2);

	BinaryArithmeticSqmExpression visitArithmetic(
			BinaryArithmeticSqmExpression.Operation operation,
			JpaExpression<?> expression1,
			JpaExpression<?> expression2,
			BasicType resultType);

	SqmFrom visitIdentificationVariableReference(JpaFrom<?,?> reference);
	SingularAttributeReference visitAttributeReference(JpaFrom<?,?> attributeSource, String attributeName);

	GenericFunctionSqmExpression visitFunction(String name, BasicType resultTypeDescriptor, List<JpaExpression<?>> arguments);
	GenericFunctionSqmExpression visitFunction(String name, BasicType resultTypeDescriptor, JpaExpression<?>... arguments);

	AvgFunctionSqmExpression visitAvgFunction(JpaExpression<?> expression, boolean distinct);
	AvgFunctionSqmExpression visitAvgFunction(JpaExpression<?> expression, boolean distinct, BasicType resultType);

	CountFunctionSqmExpression visitCountFunction(JpaExpression<?> expression, boolean distinct);
	CountFunctionSqmExpression visitCountFunction(JpaExpression<?> expression, boolean distinct, BasicType resultType);

	CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct);
	CountStarFunctionSqmExpression visitCountStarFunction(boolean distinct, BasicType resultType);

	MaxFunctionSqmExpression visitMaxFunction(JpaExpression<?> expression, boolean distinct);
	MaxFunctionSqmExpression visitMaxFunction(JpaExpression<?> expression, boolean distinct, BasicType resultType);

	MinFunctionSqmExpression visitMinFunction(JpaExpression<?> expression, boolean distinct);
	MinFunctionSqmExpression visitMinFunction(JpaExpression<?> expression, boolean distinct, BasicType resultType);

	SumFunctionSqmExpression visitSumFunction(JpaExpression<?> expression, boolean distinct);
	SumFunctionSqmExpression visitSumFunction(JpaExpression<?> expression, boolean distinct, BasicType resultType);

	ConcatSqmExpression visitConcat(
			JpaExpression<?> expression1,
			JpaExpression<?> expression2);

	ConcatSqmExpression visitConcat(
			JpaExpression<?> expression1,
			JpaExpression<?> expression2,
			BasicType resultType);

	EntityTypeLiteralSqmExpression visitEntityType(String identificationVariable);
	EntityTypeLiteralSqmExpression visitEntityType(String identificationVariable, String attributeName);

//	CollectionSizeFunction visitPluralAttributeSizeFunction();
//
//	CollectionValueFunction visitPluralAttributeElementBinding();
//	MapKeyFunction visitMapKeyBinding();
//	MapEntryFunction visitMapEntryFunction();

	SubQuerySqmExpression visitSubQuery(JpaSubquery jpaSubquery);

	AndSqmPredicate visitAndPredicate(List<JpaPredicate> predicates);
	OrSqmPredicate visitOrPredicate(List<JpaPredicate> predicates);

	EmptinessSqmPredicate visitEmptinessPredicate(JpaFrom attributeSource, String attributeName, boolean negated);
	MemberOfSqmPredicate visitMemberOfPredicate(JpaFrom attributeSource, String attributeName, boolean negated);

	BetweenSqmPredicate visitBetweenPredicate(
			JpaExpression<?> expression,
			JpaExpression<?> lowerBound,
			JpaExpression<?> upperBound,
			boolean negated);


	LikeSqmPredicate visitLikePredicate(
			JpaExpression<String> matchExpression,
			JpaExpression<String> pattern,
			JpaExpression<Character> escapeCharacter,
			boolean negated);

	InSubQuerySqmPredicate visitInSubQueryPredicate(
			JpaExpression<?> testExpression,
			JpaSubquery<?> subquery,
			boolean negated);

	InListSqmPredicate visitInTupleListPredicate(
			JpaExpression<?> testExpression,
			List<JpaExpression<?>> listExpressions,
			boolean negated);

	SqmExpression visitRoot(JpaRoot root);

	SqmExpression visitParameter(String name, int position, Class javaType);

	<T,C> CastFunctionSqmExpression visitCastFunction(JpaExpression<T> expressionToCast, Class<C> castTarget);

	GenericFunctionSqmExpression visitGenericFunction(String functionName, BasicType resultType, List<JpaExpression<?>> arguments);

	NegatedSqmPredicate visitNegatedPredicate(JpaPredicate affirmativePredicate);

	BooleanExpressionSqmPredicate visitBooleanExpressionPredicate(JpaExpression<Boolean> testExpression, Boolean assertValue);

	NullnessSqmPredicate visitNullnessPredicate(JpaExpression<?> testExpression);

	RelationalSqmPredicate visitRelationalPredicate(
			RelationalPredicateOperator operator,
			JpaExpression<?> lhs,
			JpaExpression<?> rhs);

	void visitDynamicInstantiation(Class target, List<JpaExpression<?>> arguments);
}
