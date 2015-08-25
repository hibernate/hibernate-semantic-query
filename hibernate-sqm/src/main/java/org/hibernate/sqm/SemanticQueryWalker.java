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
import org.hibernate.sqm.query.predicate.InPredicate;
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
public interface SemanticQueryWalker<T> {
	T visitStatement(Statement statement);

	T visitSelectStatement(SelectStatement statement);

	T visitUpdateStatement(UpdateStatement statement);

	T visitDeleteStatement(DeleteStatement statement);

	T visitQuerySpec(QuerySpec querySpec);

	T visitFromClause(FromClause fromClause);

	T visitFromElementSpace(FromElementSpace fromElementSpace);

	T visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement);

	T visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement);

	T visitTreatedJoinFromElement(TreatedJoinedFromElement joinedFromElement);

	T visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement);

	T visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement);

	T visitSelectClause(SelectClause selectClause);

	T visitSelection(Selection selection);

	T visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation);

	T visitSelectList(SelectList selectList);

	T visitSelectListItem(SelectListItem selectListItem);

	T visitWhereClause(WhereClause whereClause);

	T visitGroupedPredicate(GroupedPredicate predicate);

	T visitAndPredicate(AndPredicate predicate);

	T visitOrPredicate(OrPredicate predicate);

	T visitRelationalPredicate(RelationalPredicate predicate);

	T visitIsEmptyPredicate(IsEmptyPredicate predicate);

	T visitIsNullPredicate(IsNullPredicate predicate);

	T visitBetweenPredicate(BetweenPredicate predicate);

	T visitLikePredicate(LikePredicate predicate);

	T visitMemberOfPredicate(MemberOfPredicate predicate);

	T visitNegatedPredicate(NegatedPredicate predicate);

	T visitInTupleListPredicate(InTupleListPredicate predicate);

	T visitInSubQueryPredicate(InSubQueryPredicate predicate);

	T visitOrderByClause(OrderByClause orderByClause);

	T visitSortSpecification(SortSpecification sortSpecification);

	T visitPositionalParameterExpression(PositionalParameterExpression expression);

	T visitNamedParameterExpression(NamedParameterExpression expression);

	T visitEntityTypeExpression(EntityTypeExpression expression);

	T visitUnaryOperationExpression(UnaryOperationExpression expression);

	T visitAttributeReferenceExpression(AttributeReferenceExpression expression);

	T visitFromElementReferenceExpression(FromElementReferenceExpression expression);

	T visitFunctionExpression(FunctionExpression expression);

	T visitLiteralStringExpression(LiteralStringExpression expression);

	T visitLiteralCharacterExpression(LiteralCharacterExpression expression);

	T visitLiteralDoubleExpression(LiteralDoubleExpression expression);

	T visitLiteralIntegerExpression(LiteralIntegerExpression expression);

	T visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression);

	T visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression);

	T visitLiteralFloatExpression(LiteralFloatExpression expression);

	T visitLiteralLongExpression(LiteralLongExpression expression);

	T visitLiteralTrueExpression(LiteralTrueExpression expression);

	T visitLiteralFalseExpression(LiteralFalseExpression expression);

	T visitLiteralNullExpression(LiteralNullExpression expression);

	T visitConcatExpression(ConcatExpression expression);

	T visitConstantEnumExpression(ConstantEnumExpression expression);

	T visitConstantFieldExpression(ConstantFieldExpression expression);

	T visitBinaryArithmeticExpression(BinaryArithmeticExpression expression);

	T visitSubQueryExpression(SubQueryExpression expression);
}
