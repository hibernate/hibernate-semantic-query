/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import java.util.List;

import org.hibernate.query.parser.AliasCollisionException;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.ResultVariableReferenceExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.Selection;

import org.junit.Test;

import org.hibernate.test.query.parser.ConsumerContextImpl;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Andrea Boriero
 */
public class AliasTest {

	@Test(expected = AliasCollisionException.class)
	public void testRedefiningAResultVariableInSelectionsHavingDifferentSelectionExpressions() {
		final String query = "select a.address as b, a.name as b from Anything a";
		interpretQuery( query );
	}

	@Test(expected = AliasCollisionException.class)
	public void testRedefiningAResultVariableInSelectionsHavingTheSameSelectionExpression() {
		final String query = "select a.address as b, a.address as b from Anything a";
		interpretQuery( query );
	}

	@Test
	public void testMixedResultVariableAndAttributeSelection() {
		final String query = "select o, o.basic1 as o1 from Entity o";
		final QuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, "com.acme.Entity", null );
		checkAttributeReferenceExpression( querySpec, 1, "com.acme.Entity", "basic1", "o1" );

		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );
	}

	@Test
	public void testDefiningDifferentResultVAriables() {
		final String query = "select a.basic as b, a.basic2 as c from Anything a";
		final QuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, "com.acme.Anything", "basic", "b" );
		checkAttributeReferenceExpression( querySpec, 1, "com.acme.Anything", "basic2", "c" );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
	}

	@Test
	public void testDefiningAResultVariableEqualsToAnIdentificationVariable() {
		final String query = "select a as a from Anything as a";
		final QuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, "com.acme.Anything", "a" );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
	}

	@Test
	public void testReusingAnIdentificationVariableInSelectClause() {
		final String query = "select a.basic from Anything as a";
		final QuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, "com.acme.Anything", "basic", null );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
	}

	@Test(expected = AliasCollisionException.class)
	public void testDefiningAResultVariableEqualsToAnIdentificationVariableConflict() {
		final String query = "select a.basic as a from Anything as a";
		interpretQuery( query );
	}

	@Test
	public void testMultipleFromClauseSpaces() {
		final String query = "select a as a, b.basic2 from Anything as a, SomethingElse as b where b.basic = 2 ";
		final QuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, "com.acme.Anything", "a" );
		checkAttributeReferenceExpression( querySpec, 1, "com.acme.SomethingElse", "basic2", null );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
		checkFromClause( querySpec, 1, "com.acme.SomethingElse", "b" );

		checkRelationalPredicateLeftHandWhereExpression( querySpec, "com.acme.SomethingElse", "basic", "b" );
	}

	@Test(expected = AliasCollisionException.class)
	public void testDefiningAResultVariableEqualsToAnIdentificationVariableConflictWithMultipleFromClauseSpaces() {
		final String query = "select a as a from Anything as a, SomethingElse as a ";
		interpretQuery( query );
	}

	@Test
	public void testDifferentIdentificationVariablesInSubquery() {
		final String query = "select a from Anything a where a.b in ( select b from SomethingElse b where b.basic = 5)";
		final SelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, "com.acme.Anything", null );
		checkFromClause( selectStatement.getQuerySpec(), 0, "com.acme.Anything", "a" );

		final QuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, "com.acme.SomethingElse", null );
		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );
		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic", "b" );
	}

	@Test
	public void testSameIdentificationVariablesInSubquery() {
		final String query = "select a from Anything a where a.basic1 in ( select a from SomethingElse a where a.basic = 5)";
		final SelectStatement selectStatement = interpretQuery( query );

		QuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );

		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic1", "a" );

		QuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, "com.acme.SomethingElse", null );

		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "a" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic", "a" );
	}

	@Test
	public void testSubqueryUsingIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic from SomethingElse b where a.basic = b.basic2 )";
		final SelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, "com.acme.Anything", null );
		checkFromClause( selectStatement.getQuerySpec(), 0, "com.acme.Anything", "a" );

		checkInSubqueryTestExpression( selectStatement.getQuerySpec(), "com.acme.Anything", "basic", "a" );
		QuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.SomethingElse", "basic", null );

		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.Anything", "basic", "a" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic2", "b" );
	}

	@Test
	public void testSubqueriesRedefiningIdentificationVariableDefinedInparentQuery() {
		final String query = "select a.basic from Anything a where a.basic in" +
				" ( select b.basic2 from SomethingElse b where b.basic2 = a.basic1 ) and a.basic in" +
				" ( select b.basic1 from Something b where b.basic1 = a.basic )";
		final SelectStatement selectStatement = interpretQuery( query );

		QuerySpec subQuerySpec = getLeftAndPredicateSubQueryExpression( selectStatement.getQuerySpec() ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.SomethingElse", "basic2", null );
		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic2", "b" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, "com.acme.Anything", "basic1", "a" );

		subQuerySpec = getRightAndPredicateSubQueryExpression( selectStatement.getQuerySpec() ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.Something", "basic1", null );
		checkFromClause( subQuerySpec, 0, "com.acme.Something", "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.Something", "basic1", "b" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, "com.acme.Anything", "basic", "a" );
	}

	@Test
	public void testNestedSubqueriesUsingIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic1 from SomethingElse b where b.basic = " +
				"( select c.basic3 as d from Something c where c.basic3 = a.basic))";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic", "a" );

		QuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.SomethingElse", "basic1", null );
		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );

		subQuerySpec = getRelationaSubQueryExpression( subQuerySpec ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.Something", "basic3", "d" );
		checkFromClause( subQuerySpec, 0, "com.acme.Something", "c" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.Something", "basic3", "c" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, "com.acme.Anything", "basic", "a" );
	}

	@Test
	public void testNestedSubqueriesRedefiningIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic1 from SomethingElse b where b.basic = " +
				"( select a.basic3 as d from Something a where a.basic3 = a.basic ))";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic", "a" );

		QuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.SomethingElse", "basic1", null );
		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );

		subQuerySpec = getRelationaSubQueryExpression( subQuerySpec ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, "com.acme.Something", "basic3", "d" );
		checkFromClause( subQuerySpec, 0, "com.acme.Something", "a" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.Something", "basic3", "a" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, "com.acme.Something", "basic", "a" );
	}

	@Test(expected = AliasCollisionException.class)
	public void testResultVariableCollisionSubQuery() {
		final String query = "select a.address as b, a.basic as c from Anything a where a.basic2 in " +
				"(select b.basic3 as b from SomethingElse as b where b.basic3 = 2)";
		interpretQuery( query );
	}

	@Test(expected = AliasCollisionException.class)
	public void testIdentificationVariableCollisionSubQuery() {
		final String query = "select a.address as b, a.basic as c from Anything a where a.basic2 in " +
				"(select b.basic3 as e from SomethingElse as b, Something as b)";
		interpretQuery( query );
	}

	@Test(expected = AliasCollisionException.class)
	public void testReDefineSameIdentificationVariableInJoin() {
		final String query = "select a from Something a left outer join a.entity a on a.basic1 > 5";
		interpretQuery( query );
	}


	// order by

	@Test
	public void testIdentificationVariableReferencedInOrderBy() {
		final String query = "select o from Entity o order by o";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();

		checkElementSelection( querySpec, 0, "com.acme.Entity", null );
		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );

		OrderByClause orderByClause = selectStatement.getOrderByClause();
		SortSpecification sortSpecification = orderByClause.getSortSpecifications().get( 0 );
		Expression sortExpression = sortSpecification.getSortExpression();
		String collation = sortSpecification.getCollation();
		TypeDescriptor typeDescriptor = sortExpression.getTypeDescriptor();
		assertThat( typeDescriptor.getTypeName(), is( "com.acme.Entity" ) );
	}

	@Test
	public void testSelectAttributeReferencedInOrderBy() {
		final String query = "select o.basic from Entity o order by o.basic";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, "com.acme.Entity", "basic", null );
		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );

		OrderByClause orderByClause = selectStatement.getOrderByClause();
		SortSpecification sortSpecification = orderByClause.getSortSpecifications().get( 0 );
		AttributeReferenceExpression sortExpression = (AttributeReferenceExpression) sortSpecification.getSortExpression();
		TypeDescriptor typeDescriptor = sortExpression.getSource().getTypeDescriptor();
		assertThat( typeDescriptor.getTypeName(), is( "com.acme.Entity" ) );
		String attributeName = sortExpression.getAttributeDescriptor().getName();

		assertThat( attributeName, is( "basic" ) );
	}

	@Test
	public void testResultVariableReferencedInOrderBy() {
		final String query = "select o.basic as b from Entity o order by b";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, "com.acme.Entity", "basic", "b" );
		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );

		OrderByClause orderByClause = selectStatement.getOrderByClause();
		SortSpecification sortSpecification = orderByClause.getSortSpecifications().get( 0 );
		ResultVariableReferenceExpression sortExpression = (ResultVariableReferenceExpression) sortSpecification.getSortExpression();

		Selection underlyingSelection = sortExpression.getUnderlyingSelection();
		assertThat( underlyingSelection.getAlias(), is( "b" ) );

		AttributeReferenceExpression expression = (AttributeReferenceExpression) underlyingSelection.getExpression();
		assertThat( expression.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( expression.getAttributeDescriptor().getName(), is( "basic" ) );
	}

	@Test
	public void testBinaryArithmeticExpressionReferenceInOrderBy() {
		final String query = "select o.basic + o.basic1 as b from Entity o order by b";
		final SelectStatement selectStatement = interpretQuery( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();

		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );

		OrderByClause orderByClause = selectStatement.getOrderByClause();
		SortSpecification sortSpecification = orderByClause.getSortSpecifications().get( 0 );
		ResultVariableReferenceExpression sortExpression = (ResultVariableReferenceExpression) sortSpecification.getSortExpression();
		Selection underlyingSelection = sortExpression.getUnderlyingSelection();
		assertThat( underlyingSelection.getAlias(), is( "b" ) );

		BinaryArithmeticExpression expression = (BinaryArithmeticExpression) underlyingSelection.getExpression();
		AttributeReferenceExpression leftHandOperand = (AttributeReferenceExpression) expression.getLeftHandOperand();
		assertThat( leftHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( leftHandOperand.getAttributeDescriptor().getName(), is( "basic" ) );

		AttributeReferenceExpression rightHandOperand = (AttributeReferenceExpression) expression.getRightHandOperand();
		assertThat( rightHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( rightHandOperand.getAttributeDescriptor().getName(), is( "basic1" ) );
	}

	private void checkFromClause(QuerySpec querySpec, int fromClauseIndex, String typeName, String alias) {
		FromClause fromClause = querySpec.getFromClause();
		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( fromClauseIndex );
		RootEntityFromElement root = fromElementSpace.getRoot();
		assertThat( root.getEntityName(), is( typeName ) );
		if ( alias == null ) {
			assertThat( root.getAlias(), is( nullValue() ) );
		}
		else {
			assertThat( root.getAlias(), is( alias ) );
		}
	}

	private void checkAttributeReferenceExpression(
			QuerySpec querySpect,
			int attributeIndex,
			String typeName,
			String attributeName,
			String alias) {
		List<Selection> selections = querySpect.getSelectClause().getSelections();
		Selection selection = selections.get( attributeIndex );
		AttributeReferenceExpression expression = (AttributeReferenceExpression) selection.getExpression();
		assertThat( expression.getSource().getTypeDescriptor().getTypeName(), is( typeName ) );
		assertThat( expression.getAttributeDescriptor().getName(), is( attributeName ) );
		if ( alias == null ) {
			assertThat( selection.getAlias(), is( nullValue() ) );
		}
		else {
			assertThat( selection.getAlias(), is( alias ) );
		}
	}

	private void checkElementSelection(QuerySpec querySpec, int selectionIndex, String typeName, String alias) {
		List<Selection> selections = querySpec.getSelectClause().getSelections();
		Selection selection = selections.get( selectionIndex );
		assertThat( selection.getExpression().getTypeDescriptor().getTypeName(), is( typeName ) );
		if ( alias == null ) {
			assertThat( selection.getAlias(), is( nullValue() ) );
		}
		else {
			assertThat( selection.getAlias(), is( alias ) );
		}
	}

	private void checkInSubqueryTestExpression(
			QuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias
	) {
		WhereClause whereClause = querySpec.getWhereClause();
		InSubQueryPredicate predicate = (InSubQueryPredicate) whereClause.getPredicate();
		AttributeReferenceExpression testExpression = (AttributeReferenceExpression) predicate.getTestExpression();
		assertThat( testExpression.getSource().getTypeDescriptor().getTypeName(), is( typeName ) );
		assertThat( testExpression.getAttributeDescriptor().getName(), is( attributeName ) );
		assertThat( testExpression.getSource().getAlias(), is( alias ) );
	}

	private void checkRelationalPredicateLeftHandWhereExpression(
			QuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias) {
		WhereClause whereClause = querySpec.getWhereClause();
		RelationalPredicate predicate = (RelationalPredicate) whereClause.getPredicate();
		AttributeReferenceExpression leftHandExpression = (AttributeReferenceExpression) predicate.getLeftHandExpression();
		assertThat( leftHandExpression.getSource().getTypeDescriptor().getTypeName(), is( typeName ) );
		assertThat( leftHandExpression.getAttributeDescriptor().getName(), is( attributeName ) );
		assertThat( leftHandExpression.getSource().getAlias(), is( alias ) );
	}

	private void checkRelationalPredicateRightHandWhereExpression(
			QuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias) {
		WhereClause whereClause = querySpec.getWhereClause();
		RelationalPredicate predicate = (RelationalPredicate) whereClause.getPredicate();
		AttributeReferenceExpression leftHandExpression = (AttributeReferenceExpression) predicate.getRightHandExpression();
		assertThat( leftHandExpression.getAttributeDescriptor().getName(), is( attributeName ) );
		assertThat( leftHandExpression.getSource().getAlias(), is( alias ) );
		assertThat( leftHandExpression.getSource().getTypeDescriptor().getTypeName(), is( typeName ) );
	}

	private SubQueryExpression getInSubQueryExpression(SelectStatement selectStatement) {
		return getInSubQueryExpression( selectStatement.getQuerySpec() );
	}

	private SubQueryExpression getInSubQueryExpression(QuerySpec querySpec) {
		WhereClause whereClause = querySpec.getWhereClause();
		InSubQueryPredicate predicate = (InSubQueryPredicate) whereClause.getPredicate();

		return predicate.getSubQueryExpression();
	}

	private SubQueryExpression getLeftAndPredicateSubQueryExpression(QuerySpec querySpec) {
		WhereClause whereClause = querySpec.getWhereClause();
		AndPredicate predicate = (AndPredicate) whereClause.getPredicate();

		return ((InSubQueryPredicate) predicate.getLeftHandPredicate()).getSubQueryExpression();
	}

	private SubQueryExpression getRightAndPredicateSubQueryExpression(QuerySpec querySpec) {
		WhereClause whereClause = querySpec.getWhereClause();
		AndPredicate predicate = (AndPredicate) whereClause.getPredicate();

		return ((InSubQueryPredicate) predicate.getRightHandPredicate()).getSubQueryExpression();
	}

	private SubQueryExpression getRelationaSubQueryExpression(QuerySpec querySpec) {
		WhereClause whereClause = querySpec.getWhereClause();
		RelationalPredicate predicate = (RelationalPredicate) whereClause.getPredicate();
		return (SubQueryExpression) predicate.getRightHandExpression();
	}

	private SelectStatement interpretQuery(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextImpl()
		);
	}
}

