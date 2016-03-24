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
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.function.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CaseSearchedExpression;
import org.hibernate.sqm.query.expression.CaseSimpleExpression;
import org.hibernate.sqm.query.expression.function.CastFunctionExpression;
import org.hibernate.sqm.query.expression.CoalesceExpression;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.CollectionValuePathExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.function.CountFunction;
import org.hibernate.sqm.query.expression.function.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.function.GenericFunctionExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleExpression;
import org.hibernate.sqm.query.expression.LiteralFalseExpression;
import org.hibernate.sqm.query.expression.LiteralFloatExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralLongExpression;
import org.hibernate.sqm.query.expression.LiteralNullExpression;
import org.hibernate.sqm.query.expression.LiteralStringExpression;
import org.hibernate.sqm.query.expression.LiteralTrueExpression;
import org.hibernate.sqm.query.expression.MapEntryFunction;
import org.hibernate.sqm.query.expression.MapKeyPathExpression;
import org.hibernate.sqm.query.expression.MaxElementFunction;
import org.hibernate.sqm.query.expression.function.MaxFunction;
import org.hibernate.sqm.query.expression.MaxIndexFunction;
import org.hibernate.sqm.query.expression.MinElementFunction;
import org.hibernate.sqm.query.expression.function.MinFunction;
import org.hibernate.sqm.query.expression.MinIndexFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.NullifExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.function.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.BooleanExpressionPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InListPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
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
		for ( AttributeReferenceExpression stateField : statement.getStateFields() ) {
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
	public T visitGroupedPredicate(GroupedPredicate predicate) {
		predicate.getSubPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitAndPredicate(AndPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitOrPredicate(OrPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitRelationalPredicate(RelationalPredicate predicate) {
		predicate.getLeftHandExpression().accept( this );
		predicate.getRightHandExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitIsEmptyPredicate(EmptinessPredicate predicate) {
		predicate.getExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitIsNullPredicate(NullnessPredicate predicate) {
		predicate.getExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitBetweenPredicate(BetweenPredicate predicate) {
		predicate.getExpression().accept( this );
		predicate.getLowerBound().accept( this );
		predicate.getUpperBound().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitLikePredicate(LikePredicate predicate) {
		predicate.getMatchExpression().accept( this );
		predicate.getPattern().accept( this );
		predicate.getEscapeCharacter().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitMemberOfPredicate(MemberOfPredicate predicate) {
		predicate.getAttributeReferenceExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitNegatedPredicate(NegatedPredicate predicate) {
		predicate.getWrappedPredicate().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitInListPredicate(InListPredicate predicate) {
		predicate.getTestExpression().accept( this );
		for ( Expression expression : predicate.getListExpressions() ) {
			expression.accept( this );
		}
		return (T) predicate;
	}

	@Override
	public T visitInSubQueryPredicate(InSubQueryPredicate predicate) {
		predicate.getTestExpression().accept( this );
		predicate.getSubQueryExpression().accept( this );
		return (T) predicate;
	}

	@Override
	public T visitBooleanExpressionPredicate(BooleanExpressionPredicate predicate) {
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
	public T visitPositionalParameterExpression(PositionalParameterExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitNamedParameterExpression(NamedParameterExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitEntityTypeExpression(EntityTypeExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitUnaryOperationExpression(UnaryOperationExpression expression) {
		expression.getOperand().accept( this );
		return (T) expression;
	}

	@Override
	public T visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitGenericFunction(GenericFunctionExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitCastFunction(CastFunctionExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitAvgFunction(AvgFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCountStarFunction(CountStarFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCountFunction(CountFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitMaxFunction(MaxFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitMinFunction(MinFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitSumFunction(SumFunction expression) {
		return (T) expression;
	}

	@Override
	public T visitCollectionSizeFunction(CollectionSizeFunction function) {
		return (T) function;
	}

	@Override
	public T visitCollectionValueFunction(CollectionValuePathExpression function) {
		return (T) function;
	}

	@Override
	public T visitCollectionIndexFunction(CollectionIndexFunction function) {
		return (T) function;
	}

	@Override
	public T visitMapKeyFunction(MapKeyPathExpression function) {
		return (T) function;
	}

	@Override
	public T visitMapEntryFunction(MapEntryFunction function) {
		return (T) function;
	}

	@Override
	public T visitMaxElementFunction(MaxElementFunction function) {
		return (T) function;
	}

	@Override
	public T visitMinElementFunction(MinElementFunction function) {
		return (T) function;
	}

	@Override
	public T visitMaxIndexFunction(MaxIndexFunction function) {
		return (T) function;
	}

	@Override
	public T visitMinIndexFunction(MinIndexFunction function) {
		return (T) function;
	}

	@Override
	public T visitLiteralStringExpression(LiteralStringExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralFloatExpression(LiteralFloatExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralLongExpression(LiteralLongExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralTrueExpression(LiteralTrueExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralFalseExpression(LiteralFalseExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitLiteralNullExpression(LiteralNullExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitConcatExpression(ConcatExpression expression) {
		expression.getLeftHandOperand().accept( this );
		expression.getRightHandOperand().accept( this );
		return (T) expression;
	}

	@Override
	public T visitConstantEnumExpression(ConstantEnumExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitConstantFieldExpression(ConstantFieldExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitBinaryArithmeticExpression(BinaryArithmeticExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSubQueryExpression(SubQueryExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSimpleCaseExpression(CaseSimpleExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitSearchedCaseExpression(CaseSearchedExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitCoalesceExpression(CoalesceExpression expression) {
		return (T) expression;
	}

	@Override
	public T visitNullifExpression(NullifExpression expression) {
		return (T) expression;
	}
}
