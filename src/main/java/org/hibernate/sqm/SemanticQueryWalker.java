/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm;

import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.CollectionIndexSqmExpression;
import org.hibernate.sqm.query.expression.CollectionSizeSqmExpression;
import org.hibernate.sqm.query.expression.CollectionValuePathSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.function.GenericFunctionSqmExpression;
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
import org.hibernate.sqm.query.expression.MapEntrySqmExpression;
import org.hibernate.sqm.query.expression.MapKeyPathSqmExpression;
import org.hibernate.sqm.query.expression.MaxElementSqmExpression;
import org.hibernate.sqm.query.expression.function.LowerFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.MaxIndexSqmExpression;
import org.hibernate.sqm.query.expression.MinElementSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.MinIndexSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.function.TrimFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.UpperFunctionSqmExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionSqmPredicate;
import org.hibernate.sqm.query.predicate.EmptinessSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.MemberOfSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.sqm.query.set.Assignment;
import org.hibernate.sqm.query.set.SetClause;

/**
 * @author Steve Ebersole
 */
public interface SemanticQueryWalker<T> {
	T visitStatement(Statement statement);

	T visitUpdateStatement(UpdateStatement statement);

	T visitSetClause(SetClause setClause);

	T visitAssignment(Assignment assignment);

	T visitInsertSelectStatement(InsertSelectStatement statement);

	T visitDeleteStatement(DeleteStatement statement);

	T visitSelectStatement(SelectStatement statement);

	T visitQuerySpec(QuerySpec querySpec);

	T visitFromClause(FromClause fromClause);

	T visitFromElementSpace(FromElementSpace fromElementSpace);

	T visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement);

	T visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement);

	T visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement);

	T visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement);

	T visitSelectClause(SelectClause selectClause);

	T visitSelection(Selection selection);

	T visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation);

	T visitWhereClause(WhereClause whereClause);

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

	T visitEntityTypeExpression(EntityTypeSqmExpression expression);

	T visitUnaryOperationExpression(UnaryOperationSqmExpression expression);

	T visitAttributeReferenceExpression(AttributeReferenceSqmExpression expression);

	T visitGenericFunction(GenericFunctionSqmExpression expression);

	T visitCastFunction(CastFunctionSqmExpression expression);

	T visitAvgFunction(AvgFunctionSqmExpression expression);

	T visitCountStarFunction(CountStarFunctionSqmExpression expression);

	T visitCountFunction(CountFunctionSqmExpression expression);

	T visitMaxFunction(MaxFunctionSqmExpression expression);

	T visitMinFunction(MinFunctionSqmExpression expression);

	T visitSumFunction(SumFunctionSqmExpression expression);

	T visitCollectionSizeFunction(CollectionSizeSqmExpression function);

	T visitCollectionValueFunction(CollectionValuePathSqmExpression function);

	T visitCollectionIndexFunction(CollectionIndexSqmExpression function);

	T visitMapKeyFunction(MapKeyPathSqmExpression function);

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
