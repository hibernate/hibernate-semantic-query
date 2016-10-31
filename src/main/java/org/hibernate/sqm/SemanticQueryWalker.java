/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmInsertSelectStatement;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.CollectionIndexSqmExpression;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeLiteralSqmExpression;
import org.hibernate.sqm.query.expression.domain.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.domain.MapEntrySqmExpression;
import org.hibernate.sqm.query.expression.domain.MaxElementSqmExpression;
import org.hibernate.sqm.query.expression.domain.MaxIndexSqmExpression;
import org.hibernate.sqm.query.expression.domain.MinElementSqmExpression;
import org.hibernate.sqm.query.expression.domain.MinIndexSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.expression.ParameterizedEntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.MapKeyBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeElementBinding;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.LowerFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.TrimFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.UpperFunctionSqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;
import org.hibernate.sqm.query.set.SqmAssignment;
import org.hibernate.sqm.query.set.SqmSetClause;

/**
 * @author Steve Ebersole
 */
public interface SemanticQueryWalker<T> {
	T visitStatement(SqmStatement statement);

	T visitUpdateStatement(SqmUpdateStatement statement);

	T visitSetClause(SqmSetClause setClause);

	T visitAssignment(SqmAssignment assignment);

	T visitInsertSelectStatement(SqmInsertSelectStatement statement);

	T visitDeleteStatement(SqmDeleteStatement statement);

	T visitSelectStatement(SqmSelectStatement statement);

	T visitQuerySpec(SqmQuerySpec querySpec);

	T visitFromClause(SqmFromClause fromClause);

	T visitFromElementSpace(FromElementSpace fromElementSpace);

	T visitRootEntityFromElement(SqmRoot rootEntityFromElement);

	T visitCrossJoinedFromElement(SqmCrossJoin joinedFromElement);

	T visitQualifiedEntityJoinFromElement(SqmEntityJoin joinedFromElement);

	T visitQualifiedAttributeJoinFromElement(SqmAttributeJoin joinedFromElement);

	T visitSelectClause(SqmSelectClause selectClause);

	T visitSelection(SqmSelection selection);

	T visitDynamicInstantiation(SqmDynamicInstantiation dynamicInstantiation);

	T visitWhereClause(SqmWhereClause whereClause);

	T visitGroupedPredicate(GroupedSqmPredicate predicate);

	T visitAndPredicate(AndSqmPredicate predicate);

	T visitOrPredicate(OrSqmPredicate predicate);

	T visitRelationalPredicate(RelationalSqmPredicate predicate);

	T visitIsEmptyPredicate(EmptinessSqmPredicate predicate);

	T visitIsNullPredicate(NullnessSqmPredicate predicate);

	T visitBetweenPredicate(BetweenSqmPredicate predicate);

	T visitLikePredicate(LikeSqmPredicate predicate);

	T visitMemberOfPredicate(MemberOfSqmPredicate predicate);

	T visitNegatedPredicate(NegatedSqmPredicate predicate);

	T visitInListPredicate(InListSqmPredicate predicate);

	T visitInSubQueryPredicate(InSubQuerySqmPredicate predicate);

	T visitBooleanExpressionPredicate(BooleanExpressionSqmPredicate predicate);

	T visitOrderByClause(OrderByClause orderByClause);

	T visitSortSpecification(SortSpecification sortSpecification);

	T visitPositionalParameterExpression(PositionalParameterSqmExpression expression);

	T visitNamedParameterExpression(NamedParameterSqmExpression expression);

	T visitEntityTypeLiteralExpression(EntityTypeLiteralSqmExpression expression);

	T visitEntityTypeExpression(EntityTypeSqmExpression expression);

	T visitParameterizedEntityTypeExpression(ParameterizedEntityTypeSqmExpression expression);

	T visitUnaryOperationExpression(UnaryOperationSqmExpression expression);

	T visitAttributeReferenceExpression(AttributeBinding expression);

	T visitGenericFunction(GenericFunctionSqmExpression expression);

	T visitCastFunction(CastFunctionSqmExpression expression);

	T visitAvgFunction(AvgFunctionSqmExpression expression);

	T visitCountStarFunction(CountStarFunctionSqmExpression expression);

	T visitCountFunction(CountFunctionSqmExpression expression);

	T visitMaxFunction(MaxFunctionSqmExpression expression);

	T visitMinFunction(MinFunctionSqmExpression expression);

	T visitSumFunction(SumFunctionSqmExpression expression);

	T visitCollectionSizeFunction(CollectionSizeSqmExpression function);

	T visitCollectionValueBinding(PluralAttributeElementBinding binding);

	T visitCollectionIndexFunction(CollectionIndexSqmExpression function);

	T visitMapKeyBinding(MapKeyBinding binding);

	T visitMapEntryFunction(MapEntrySqmExpression function);

	T visitMaxElementFunction(MaxElementSqmExpression function);

	T visitMinElementFunction(MinElementSqmExpression function);

	T visitMaxIndexFunction(MaxIndexSqmExpression function);

	T visitMinIndexFunction(MinIndexSqmExpression function);

	T visitLiteralStringExpression(LiteralStringSqmExpression expression);

	T visitLiteralCharacterExpression(LiteralCharacterSqmExpression expression);

	T visitLiteralDoubleExpression(LiteralDoubleSqmExpression expression);

	T visitLiteralIntegerExpression(LiteralIntegerSqmExpression expression);

	T visitLiteralBigIntegerExpression(LiteralBigIntegerSqmExpression expression);

	T visitLiteralBigDecimalExpression(LiteralBigDecimalSqmExpression expression);

	T visitLiteralFloatExpression(LiteralFloatSqmExpression expression);

	T visitLiteralLongExpression(LiteralLongSqmExpression expression);

	T visitLiteralTrueExpression(LiteralTrueSqmExpression expression);

	T visitLiteralFalseExpression(LiteralFalseSqmExpression expression);

	T visitLiteralNullExpression(LiteralNullSqmExpression expression);

	T visitConcatExpression(ConcatSqmExpression expression);

	T visitConcatFunction(ConcatFunctionSqmExpression expression);

	T visitConstantEnumExpression(ConstantEnumSqmExpression expression);

	T visitConstantFieldExpression(ConstantFieldSqmExpression expression);

	T visitBinaryArithmeticExpression(BinaryArithmeticSqmExpression expression);

	T visitSubQueryExpression(SubQuerySqmExpression expression);

	T visitSimpleCaseExpression(CaseSimpleSqmExpression expression);

	T visitSearchedCaseExpression(CaseSearchedSqmExpression expression);

	T visitCoalesceExpression(CoalesceSqmExpression expression);

	T visitNullifExpression(NullifSqmExpression expression);

	T visitSubstringFunction(SubstringFunctionSqmExpression expression);

	T visitTrimFunction(TrimFunctionSqmExpression expression);

	T visitUpperFunction(UpperFunctionSqmExpression expression);

	T visitLowerFunction(LowerFunctionSqmExpression expression);
}
