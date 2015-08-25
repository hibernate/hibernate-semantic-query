/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm;

import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
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
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
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
import org.hibernate.sqm.query.predicate.IsEmptyPredicate;
import org.hibernate.sqm.query.predicate.IsNullPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.SelectList;
import org.hibernate.sqm.query.select.SelectListItem;
import org.hibernate.sqm.query.select.Selection;

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
		visitWhereClause( statement.getWhereClause() );
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
		visitSelection( selectClause.getSelection() );
		return selectClause;
	}

	@Override
	public Selection visitSelection(Selection selection) {
		return (Selection) selection.accept( this );
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation) {
		return dynamicInstantiation;
	}

	@Override
	public SelectList visitSelectList(SelectList selectList) {
		for ( SelectListItem selectListItem : selectList.getSelectListItems() ) {
			visitSelectListItem( selectListItem );
		}
		return selectList;
	}

	@Override
	public SelectListItem visitSelectListItem(SelectListItem selectListItem) {
		selectListItem.getSelectedExpression().accept( this );
		return selectListItem;
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
	public IsEmptyPredicate visitIsEmptyPredicate(IsEmptyPredicate predicate) {
		predicate.getExpression().accept( this );
		return predicate;
	}

	@Override
	public IsNullPredicate visitIsNullPredicate(IsNullPredicate predicate) {
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
}
