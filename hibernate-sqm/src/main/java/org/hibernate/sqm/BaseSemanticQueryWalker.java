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
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CollectionIndexFunction;
import org.hibernate.sqm.query.expression.CollectionSizeFunction;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.FunctionExpression;
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
import org.hibernate.sqm.query.expression.MapKeyFunction;
import org.hibernate.sqm.query.expression.MaxElementFunction;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MaxIndexFunction;
import org.hibernate.sqm.query.expression.MinElementFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.MinIndexFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.ResultVariableReferenceExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.from.TreatedJoinedFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
import org.hibernate.sqm.query.predicate.EmptinessPredicate;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
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
public class BaseSemanticQueryWalker implements SemanticQueryWalker {
	@Override
	public Statement visitStatement(Statement statement) {
		return (Statement) statement.accept( this );
	}

	@Override
	public SelectStatement visitSelectStatement(SelectStatement statement) {
		visitQuerySpec( statement.getQuerySpec() );
		visitOrderByClause( statement.getOrderByClause() );
		return statement;
	}

	@Override
	public UpdateStatement visitUpdateStatement(UpdateStatement statement) {
		visitRootEntityFromElement( statement.getEntityFromElement() );
		visitSetClause( statement.getSetClause() );
		visitWhereClause( statement.getWhereClause() );
		return statement;
	}

	@Override
	public SetClause visitSetClause(SetClause setClause) {
		for ( Assignment assignment : setClause.getAssignments() ) {
			visitAssignment( assignment );
		}
		return setClause;
	}

	@Override
	public Assignment visitAssignment(Assignment assignment) {
		visitAttributeReferenceExpression( assignment.getStateField() );
		assignment.getStateField().accept( this );
		return assignment;
	}

	@Override
	public InsertSelectStatement visitInsertSelectStatement(InsertSelectStatement statement) {
		visitRootEntityFromElement( statement.getInsertTarget() );
		for ( AttributeReferenceExpression stateField : statement.getStateFields() ) {
			stateField.accept( this );
		}
		visitQuerySpec( statement.getSelectQuery() );
		return statement;
	}

	@Override
	public DeleteStatement visitDeleteStatement(DeleteStatement statement) {
		visitRootEntityFromElement( statement.getEntityFromElement() );
		visitWhereClause( statement.getWhereClause() );
		return statement;
	}

	@Override
	public QuerySpec visitQuerySpec(QuerySpec querySpec) {
		visitSelectClause( querySpec.getSelectClause() );
		visitFromClause( querySpec.getFromClause() );
		visitWhereClause( querySpec.getWhereClause() );
		return querySpec;
	}

	@Override
	public FromClause visitFromClause(FromClause fromClause) {
		for ( FromElementSpace fromElementSpace : fromClause.getFromElementSpaces() ) {
			visitFromElementSpace( fromElementSpace );
		}
		return fromClause;
	}

	@Override
	public FromElementSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
		visitRootEntityFromElement( fromElementSpace.getRoot() );
		for ( JoinedFromElement joinedFromElement : fromElementSpace.getJoins() ) {
			joinedFromElement.accept( this );
		}
		return fromElementSpace;
	}

	@Override
	public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
		return joinedFromElement;
	}

	@Override
	public Object visitTreatedJoinFromElement(TreatedJoinedFromElement joinedFromElement) {
		return joinedFromElement;
	}

	@Override
	public Object visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
		return joinedFromElement;
	}

	@Override
	public Object visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
		return joinedFromElement;
	}

	@Override
	public RootEntityFromElement visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
		return rootEntityFromElement;
	}

	@Override
	public SelectClause visitSelectClause(SelectClause selectClause) {
		for ( Selection selection : selectClause.getSelections() ) {
			visitSelection( selection );
		}
		return selectClause;
	}

	@Override
	public Selection visitSelection(Selection selection) {
		selection.getExpression().accept( this );
		return selection;
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation) {
		return dynamicInstantiation;
	}

	@Override
	public WhereClause visitWhereClause(WhereClause whereClause) {
		whereClause.getPredicate().accept( this );
		return whereClause;
	}

	@Override
	public GroupedPredicate visitGroupedPredicate(GroupedPredicate predicate) {
		predicate.getSubPredicate().accept( this );
		return predicate;
	}

	@Override
	public AndPredicate visitAndPredicate(AndPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return predicate;
	}

	@Override
	public OrPredicate visitOrPredicate(OrPredicate predicate) {
		predicate.getLeftHandPredicate().accept( this );
		predicate.getRightHandPredicate().accept( this );
		return predicate;
	}

	@Override
	public RelationalPredicate visitRelationalPredicate(RelationalPredicate predicate) {
		predicate.getLeftHandExpression().accept( this );
		predicate.getRightHandExpression().accept( this );
		return predicate;
	}

	@Override
	public EmptinessPredicate visitIsEmptyPredicate(EmptinessPredicate predicate) {
		predicate.getExpression().accept( this );
		return predicate;
	}

	@Override
	public NullnessPredicate visitIsNullPredicate(NullnessPredicate predicate) {
		predicate.getExpression().accept( this );
		return predicate;
	}

	@Override
	public BetweenPredicate visitBetweenPredicate(BetweenPredicate predicate) {
		predicate.getExpression().accept( this );
		predicate.getLowerBound().accept( this );
		predicate.getUpperBound().accept( this );
		return predicate;
	}

	@Override
	public LikePredicate visitLikePredicate(LikePredicate predicate) {
		predicate.getMatchExpression().accept( this );
		predicate.getPattern().accept( this );
		predicate.getEscapeCharacter().accept( this );
		return predicate;
	}

	@Override
	public MemberOfPredicate visitMemberOfPredicate(MemberOfPredicate predicate) {
		predicate.getAttributeReferenceExpression().accept( this );
		return predicate;
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(NegatedPredicate predicate) {
		predicate.getWrappedPredicate().accept( this );
		return predicate;
	}

	@Override
	public InTupleListPredicate visitInTupleListPredicate(InTupleListPredicate predicate) {
		predicate.getTestExpression().accept( this );
		for ( Expression expression : predicate.getTupleListExpressions() ) {
			expression.accept( this );
		}
		return predicate;
	}

	@Override
	public InSubQueryPredicate visitInSubQueryPredicate(InSubQueryPredicate predicate) {
		predicate.getTestExpression().accept( this );
		predicate.getSubQueryExpression().accept( this );
		return predicate;
	}

	@Override
	public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
		for ( SortSpecification sortSpecification : orderByClause.getSortSpecifications() ) {
			visitSortSpecification( sortSpecification );
		}
		return orderByClause;
	}

	@Override
	public SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
		sortSpecification.getSortExpression().accept( this );
		return sortSpecification;
	}

	@Override
	public PositionalParameterExpression visitPositionalParameterExpression(PositionalParameterExpression expression) {
		return expression;
	}

	@Override
	public NamedParameterExpression visitNamedParameterExpression(NamedParameterExpression expression) {
		return expression;
	}

	@Override
	public EntityTypeExpression visitEntityTypeExpression(EntityTypeExpression expression) {
		return expression;
	}

	@Override
	public UnaryOperationExpression visitUnaryOperationExpression(UnaryOperationExpression expression) {
		expression.getOperand().accept( this );
		return expression;
	}

	@Override
	public AttributeReferenceExpression visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		return expression;
	}

	@Override
	public FromElementReferenceExpression visitFromElementReferenceExpression(FromElementReferenceExpression expression) {
		return expression;
	}

	@Override
	public FunctionExpression visitFunctionExpression(FunctionExpression expression) {
		return expression;
	}

	@Override
	public AvgFunction visitAvgFunction(AvgFunction expression) {
		return expression;
	}

	@Override
	public CountStarFunction visitCountStarFunction(CountStarFunction expression) {
		return expression;
	}

	@Override
	public CountFunction visitCountFunction(CountFunction expression) {
		return expression;
	}

	@Override
	public MaxFunction visitMaxFunction(MaxFunction expression) {
		return expression;
	}

	@Override
	public MinFunction visitMinFunction(MinFunction expression) {
		return expression;
	}

	@Override
	public SumFunction visitSumFunction(SumFunction expression) {
		return expression;
	}

	@Override
	public CollectionSizeFunction visitCollectionSizeFunction(CollectionSizeFunction function) {
		return function;
	}

	@Override
	public CollectionValueFunction visitCollectionValueFunction(CollectionValueFunction function) {
		return function;
	}

	@Override
	public CollectionIndexFunction visitCollectionIndexFunction(CollectionIndexFunction function) {
		return function;
	}

	@Override
	public MapKeyFunction visitMapKeyFunction(MapKeyFunction function) {
		return function;
	}

	@Override
	public MapEntryFunction visitMapEntryFunction(MapEntryFunction function) {
		return function;
	}

	@Override
	public MaxElementFunction visitMaxElementFunction(MaxElementFunction function) {
		return function;
	}

	@Override
	public MinElementFunction visitMinElementFunction(MinElementFunction function) {
		return function;
	}

	@Override
	public MaxIndexFunction visitMaxIndexFunction(MaxIndexFunction function) {
		return function;
	}

	@Override
	public MinIndexFunction visitMinIndexFunction(MinIndexFunction function) {
		return function;
	}

	@Override
	public LiteralStringExpression visitLiteralStringExpression(LiteralStringExpression expression) {
		return expression;
	}

	@Override
	public LiteralCharacterExpression visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
		return expression;
	}

	@Override
	public LiteralDoubleExpression visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
		return expression;
	}

	@Override
	public LiteralIntegerExpression visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
		return expression;
	}

	@Override
	public LiteralBigIntegerExpression visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
		return expression;
	}

	@Override
	public LiteralBigDecimalExpression visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
		return expression;
	}

	@Override
	public LiteralFloatExpression visitLiteralFloatExpression(LiteralFloatExpression expression) {
		return expression;
	}

	@Override
	public LiteralLongExpression visitLiteralLongExpression(LiteralLongExpression expression) {
		return expression;
	}

	@Override
	public LiteralTrueExpression visitLiteralTrueExpression(LiteralTrueExpression expression) {
		return expression;
	}

	@Override
	public LiteralFalseExpression visitLiteralFalseExpression(LiteralFalseExpression expression) {
		return expression;
	}

	@Override
	public LiteralNullExpression visitLiteralNullExpression(LiteralNullExpression expression) {
		return expression;
	}

	@Override
	public ConcatExpression visitConcatExpression(ConcatExpression expression) {
		expression.getLeftHandOperand().accept( this );
		expression.getRightHandOperand().accept( this );
		return expression;
	}

	@Override
	public ConstantEnumExpression visitConstantEnumExpression(ConstantEnumExpression expression) {
		return expression;
	}

	@Override
	public ConstantFieldExpression visitConstantFieldExpression(ConstantFieldExpression expression) {
		return expression;
	}

	@Override
	public BinaryArithmeticExpression visitBinaryArithmeticExpression(BinaryArithmeticExpression expression) {
		return expression;
	}

	@Override
	public SubQueryExpression visitSubQueryExpression(SubQueryExpression expression) {
		return expression;
	}

	@Override
	public ResultVariableReferenceExpression visitResultVariableReferenceExpression(ResultVariableReferenceExpression expression) {
		return expression;
	}

}
