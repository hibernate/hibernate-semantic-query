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
import org.hibernate.sqm.query.expression.function.AvgSqmFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.CollectionIndexSqmFunction;
import org.hibernate.sqm.query.expression.CollectionSizeSqmFunction;
import org.hibernate.sqm.query.expression.CollectionValuePathSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountSqmFunction;
import org.hibernate.sqm.query.expression.function.CountStarSqmFunction;
import org.hibernate.sqm.query.expression.EntityTypeSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
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
import org.hibernate.sqm.query.expression.MapEntrySqmFunction;
import org.hibernate.sqm.query.expression.MapKeyPathSqmExpression;
import org.hibernate.sqm.query.expression.MaxElementSqmFunction;
import org.hibernate.sqm.query.expression.function.LowerFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxSqmFunction;
import org.hibernate.sqm.query.expression.MaxIndexSqmFunction;
import org.hibernate.sqm.query.expression.MinElementSqmFunction;
import org.hibernate.sqm.query.expression.function.MinSqmFunction;
import org.hibernate.sqm.query.expression.MinIndexSqmFunction;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumSqmFunction;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.function.TrimFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.UpperFunctionSqmExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
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
@SuppressWarnings({"unchecked", "WeakerAccess"})
public class BaseSemanticQueryWalker<T> implements SemanticQueryWalker<T> {
	@Override
	public T visitStatement(Statement statement) {
		return statement.accept( this );
	}

	@Override
	public T visitSelectStatement(SelectStatement statement) {
		visitQuerySpec( statement.getQuerySpec() );
		visitOrderByClause( statement.getOrderByClause() );
		return (T) statement;
	}

	@Override
	public T visitUpdateStatement(UpdateStatement statement) {
		visitRootEntityFromElement( statement.getEntityFromElement() );
		visitSetClause( statement.getSetClause() );
		visitWhereClause( statement.getWhereClause() );
		return (T) statement;
	}

	@Override
	public T visitSetClause(SetClause setClause) {
		for ( Assignment assignment : setClause.getAssignments() ) {
			visitAssignment( assignment );
		}
		return (T) setClause;
	}

	@Override
	public T visitAssignment(Assignment assignment) {
		visitAttributeReferenceExpression( assignment.getStateField() );
		assignment.getStateField().accept( this );
		return (T) assignment;
	}

	@Override
	public T visitInsertSelectStatement(InsertSelectStatement statement) {
		visitRootEntityFromElement( statement.getInsertTarget() );
		for ( AttributeReferenceSqmExpression stateField : statement.getStateFields() ) {
			stateField.accept( this );
		}
		visitQuerySpec( statement.getSelectQuery() );
		return (T) statement;
	}

	@Override
	public T visitDeleteStatement(DeleteStatement statement) {
		visitRootEntityFromElement( statement.getEntityFromElement() );
		visitWhereClause( statement.getWhereClause() );
		return (T) statement;
	}

	@Override
	public T visitQuerySpec(QuerySpec querySpec) {
		visitFromClause( querySpec.getFromClause() );
		visitSelectClause( querySpec.getSelectClause() );
		visitWhereClause( querySpec.getWhereClause() );
		return (T) querySpec;
	}

	@Override
	public T visitFromClause(FromClause fromClause) {
		for ( FromElementSpace fromElementSpace : fromClause.getFromElementSpaces() ) {
			visitFromElementSpace( fromElementSpace );
		}
		return (T) fromClause;
	}

	@Override
	public T visitFromElementSpace(FromElementSpace fromElementSpace) {
		visitRootEntityFromElement( fromElementSpace.getRoot() );
		for ( JoinedFromElement joinedFromElement : fromElementSpace.getJoins() ) {
			joinedFromElement.accept( this );
		}
		return (T) fromElementSpace;
	}

	@Override
	public T visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
		return (T) joinedFromElement;
	}

	@Override
	public T visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
		return (T) joinedFromElement;
	}

	@Override
	public T visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
		return (T) joinedFromElement;
	}

	@Override
	public T visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
		return (T) rootEntityFromElement;
	}

	@Override
	public T visitSelectClause(SelectClause selectClause) {
		for ( Selection selection : selectClause.getSelections() ) {
			visitSelection( selection );
		}
		return (T) selectClause;
	}

	@Override
	public T visitSelection(Selection selection) {
		selection.getExpression().accept( this );
		return (T) selection;
	}

	@Override
	public T visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation) {
		return (T) dynamicInstantiation;
	}

	@Override
	public T visitWhereClause(WhereClause whereClause) {
		whereClause.getPredicate().accept( this );
		return (T) whereClause;
	}

	@Override
	public T visitGroupedPredicate(GroupedSqmPredicate predicate) {
		predicate.getSubPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitAndPredicate(AndSqmPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitOrPredicate(OrSqmPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitRelationalPredicate(RelationalSqmPredicate predicate) {
		predicate.getLeftHandExpression().accept( this );
		predicate.getRightHandExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitIsEmptyPredicate(EmptinessSqmPredicate predicate) {
		predicate.getExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitIsNullPredicate(NullnessSqmPredicate predicate) {
		predicate.getExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitBetweenPredicate(BetweenSqmPredicate predicate) {
		predicate.getExpression().accept( this );
		predicate.getLowerBound().accept( this );
		predicate.getUpperBound().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitLikePredicate(LikeSqmPredicate predicate) {
		predicate.getMatchExpression().accept( this );
		predicate.getPattern().accept( this );
		predicate.getEscapeCharacter().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitMemberOfPredicate(MemberOfSqmPredicate predicate) {
		predicate.getAttributeReferenceExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitNegatedPredicate(NegatedSqmPredicate predicate) {
		predicate.getWrappedPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitInListPredicate(InListSqmPredicate predicate) {
		predicate.getTestExpression().accept( this );
		for ( SqmExpression expression : predicate.getListExpressions() ) {
			expression.accept( this );
		}
		return (T) predicate;
	}

	@Override
	public T visitInSubQueryPredicate(InSubQuerySqmPredicate predicate) {
		predicate.getTestExpression().accept( this );
		predicate.getSubQueryExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitBooleanExpressionPredicate(BooleanExpressionSqmPredicate predicate) {
		predicate.getBooleanExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitOrderByClause(OrderByClause orderByClause) {
		if ( orderByClause.getSortSpecifications() != null ) {
			for ( SortSpecification sortSpecification : orderByClause.getSortSpecifications() ) {
				visitSortSpecification( sortSpecification );
			}
		}
		return (T) orderByClause;
	}

	@Override
	public T visitSortSpecification(SortSpecification sortSpecification) {
		sortSpecification.getSortExpression().accept( this );
		return (T) sortSpecification;
	}

	@Override
	public T visitPositionalParameterExpression(PositionalParameterSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitNamedParameterExpression(NamedParameterSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitEntityTypeExpression(EntityTypeSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitUnaryOperationExpression(UnaryOperationSqmExpression expression) {
		expression.getOperand().accept( this );
		return (T) expression;
	}

	@Override
	public T visitAttributeReferenceExpression(AttributeReferenceSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitGenericFunction(GenericFunctionSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitCastFunction(CastFunctionSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitAvgFunction(AvgSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCountStarFunction(CountStarSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCountFunction(CountSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitMaxFunction(MaxSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitMinFunction(MinSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitSumFunction(SumSqmFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCollectionSizeFunction(CollectionSizeSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitCollectionValueFunction(CollectionValuePathSqmExpression function) {
		return (T) function;
	}

	@Override
	public T visitCollectionIndexFunction(CollectionIndexSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitMapKeyFunction(MapKeyPathSqmExpression function) {
		return (T) function;
	}

	@Override
	public T visitMapEntryFunction(MapEntrySqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitMaxElementFunction(MaxElementSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitMinElementFunction(MinElementSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitMaxIndexFunction(MaxIndexSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitMinIndexFunction(MinIndexSqmFunction function) {
		return (T) function;
	}

	@Override
	public T visitLiteralStringExpression(LiteralStringSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralCharacterExpression(LiteralCharacterSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralDoubleExpression(LiteralDoubleSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralIntegerExpression(LiteralIntegerSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralBigIntegerExpression(LiteralBigIntegerSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralBigDecimalExpression(LiteralBigDecimalSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralFloatExpression(LiteralFloatSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralLongExpression(LiteralLongSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralTrueExpression(LiteralTrueSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralFalseExpression(LiteralFalseSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralNullExpression(LiteralNullSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitConcatExpression(ConcatSqmExpression expression) {
		expression.getLeftHandOperand().accept( this );
		expression.getRightHandOperand().accept( this );
		return (T) expression;
	}

	@Override
	public T visitConcatFunction(ConcatFunctionSqmExpression expression) {
		for ( SqmExpression argument : expression.getExpressions() ) {
			argument.accept( this );
		}
		return (T) expression;
	}

	@Override
	public T visitConstantEnumExpression(ConstantEnumSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitConstantFieldExpression(ConstantFieldSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitBinaryArithmeticExpression(BinaryArithmeticSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSubQueryExpression(SubQuerySqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSimpleCaseExpression(CaseSimpleSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSearchedCaseExpression(CaseSearchedSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitCoalesceExpression(CoalesceSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitNullifExpression(NullifSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSubstringFunction(SubstringFunctionSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitTrimFunction(TrimFunctionSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLowerFunction(LowerFunctionSqmExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitUpperFunction(UpperFunctionSqmExpression expression) {
		return (T) expression;
	}
}
