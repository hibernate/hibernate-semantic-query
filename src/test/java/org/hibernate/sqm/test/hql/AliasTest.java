/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import java.util.List;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.orm.persister.entity.spi.EntityReference;
import org.hibernate.query.sqm.produce.spi.SemanticQueryProducer;
import org.hibernate.query.sqm.AliasCollisionException;
import org.hibernate.query.sqm.produce.spi.ImplicitAliasGenerator;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SubQuerySqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.AndSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.InSubQuerySqmPredicate;
import org.hibernate.query.sqm.tree.predicate.RelationalSqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sqm.test.ConsumerContextImpl;
import org.hibernate.sqm.test.domain.OrmHelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrea Boriero
 */
public class AliasTest {

	final ConsumerContextImpl consumerContext = new ConsumerContextImpl(
			OrmHelper.buildDomainMetamodel( Entity.class, Anything.class, Something.class, SomethingElse.class )
	);

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
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, Entity.class.getName(), null );
		checkAttributeReferenceExpression( querySpec, 1, Entity.class.getName(), "basic1", "o1" );

		checkFromClause( querySpec, 0, Entity.class.getName(), "o" );
	}

	@Test
	public void testDefiningDifferentResultVAriables() {
		final String query = "select a.basic as b, a.basic2 as c from Anything a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, Anything.class.getName(), "basic", "b" );
		checkAttributeReferenceExpression( querySpec, 1, Anything.class.getName(), "basic2", "c" );

		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
	}

	@Test
	public void testDefiningAResultVariableEqualsToAnIdentificationVariable() {
		final String query = "select a as a from Anything as a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, Anything.class.getName(), "a" );

		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
	}

	@Test
	public void testReusingAnIdentificationVariableInSelectClause() {
		final String query = "select a.basic from Anything as a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, Anything.class.getName(), "basic", null );

		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
	}

	@Test(expected = AliasCollisionException.class)
	public void testDefiningAResultVariableEqualsToAnIdentificationVariableConflict() {
		final String query = "select a.basic as a from Anything as a";
		interpretQuery( query );
	}

	@Test
	public void testMultipleFromClauseSpaces() {
		final String query = "select a as a, b.basic2 from Anything as a, SomethingElse as b where b.basic = 2 ";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, Anything.class.getName(), "a" );
		checkAttributeReferenceExpression( querySpec, 1, SomethingElse.class.getName(), "basic2", null );

		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
		checkFromClause( querySpec, 1, SomethingElse.class.getName(), "b" );

		checkRelationalPredicateLeftHandWhereExpression( querySpec, SomethingElse.class.getName(), "basic", "b" );
	}

	@Test(expected = AliasCollisionException.class)
	public void testDefiningAResultVariableEqualsToAnIdentificationVariableConflictWithMultipleFromClauseSpaces() {
		final String query = "select a as a from Anything as a, SomethingElse as a ";
		interpretQuery( query );
	}

	@Test
	public void testDifferentIdentificationVariablesInSubquery() {
		final String query = "select a from Anything a where a.b in ( select b from SomethingElse b where b.basic = 5)";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, Anything.class.getName(), null );
		checkFromClause( selectStatement.getQuerySpec(), 0, Anything.class.getName(), "a" );

		final SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, SomethingElse.class.getName(), null );
		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "b" );
		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, SomethingElse.class.getName(), "basic", "b" );
	}

	@Test
	public void testSameIdentificationVariablesInSubquery() {
		final String query = "select a from Anything a where a.basic1 in ( select a from SomethingElse a where a.basic = 5)";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, Anything.class.getName(), null );
		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );

		checkInSubqueryTestExpression( querySpec, Anything.class.getName(), "basic1", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, SomethingElse.class.getName(), null );

		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "a" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, SomethingElse.class.getName(), "basic", "a" );
	}

	@Test
	public void testSubqueryUsingIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic from SomethingElse b where a.basic = b.basic2 )";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, Anything.class.getName(), null );
		checkFromClause( selectStatement.getQuerySpec(), 0, Anything.class.getName(), "a" );

		checkInSubqueryTestExpression( selectStatement.getQuerySpec(), "com.acme.Anything", "basic", "a" );
		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, SomethingElse.class.getName(), "basic", null );

		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, Anything.class.getName(), "basic", "a" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, SomethingElse.class.getName(), "basic2", "b" );
	}

	@Test
	public void testSubqueriesRedefiningIdentificationVariableDefinedInparentQuery() {
		final String query = "select a.basic from Anything a where a.basic in" +
				" ( select b.basic2 from SomethingElse b where b.basic2 = a.basic1 ) and a.basic in" +
				" ( select b.basic1 from Something b where b.basic1 = a.basic )";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		SqmQuerySpec subQuerySpec = getLeftAndPredicateSubQueryExpression( selectStatement.getQuerySpec() ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, SomethingElse.class.getName(), "basic2", null );
		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, SomethingElse.class.getName(), "basic2", "b" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, Anything.class.getName(), "basic1", "a" );

		subQuerySpec = getRightAndPredicateSubQueryExpression( selectStatement.getQuerySpec() ).getQuerySpec();
		checkAttributeReferenceExpression( subQuerySpec, 0, Something.class.getName(), "basic1", null );
		checkFromClause( subQuerySpec, 0, Something.class.getName(), "b" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, Something.class.getName(), "basic1", "b" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, Anything.class.getName(), "basic", "a" );
	}

	@Test
	public void testNestedSubqueriesUsingIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic1 from SomethingElse b where b.basic = " +
				"( select c.basic3 as d from Something c where c.basic3 = a.basic))";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, Anything.class.getName(), null );
		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
		checkInSubqueryTestExpression( querySpec, Anything.class.getName(), "basic", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, SomethingElse.class.getName(), "basic1", null );
		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "b" );

		subQuerySpec = getRelationaSubQueryExpression( subQuerySpec ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, Something.class.getName(), "basic3", "d" );
		checkFromClause( subQuerySpec, 0, Something.class.getName(), "c" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, Something.class.getName(), "basic3", "c" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, Anything.class.getName(), "basic", "a" );
	}

	@Test
	public void testNestedSubqueriesRedefiningIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic1 from SomethingElse b where b.basic = " +
				"( select a.basic3 as d from Something a where a.basic3 = a.basic ))";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, Anything.class.getName(), null );
		checkFromClause( querySpec, 0, Anything.class.getName(), "a" );
		checkInSubqueryTestExpression( querySpec, Anything.class.getName(), "basic", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, SomethingElse.class.getName(), "basic1", null );
		checkFromClause( subQuerySpec, 0, SomethingElse.class.getName(), "b" );

		subQuerySpec = getRelationaSubQueryExpression( subQuerySpec ).getQuerySpec();

		checkAttributeReferenceExpression( subQuerySpec, 0, Something.class.getName(), "basic3", "d" );
		checkFromClause( subQuerySpec, 0, Something.class.getName(), "a" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, Something.class.getName(), "basic3", "a" );
		checkRelationalPredicateRightHandWhereExpression( subQuerySpec, Something.class.getName(), "basic", "a" );
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

	private void checkFromClause(SqmQuerySpec querySpec, int fromClauseIndex, String typeName, String alias) {
		SqmFromClause fromClause = querySpec.getFromClause();
		SqmFromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( fromClauseIndex );
		SqmRoot root = fromElementSpace.getRoot();
		assertThat( root.getEntityName(), is( typeName ) );
		if ( alias == null ) {
			assertThat( root.getIdentificationVariable(), is( nullValue() ) );
		}
		else {
			assertThat( root.getIdentificationVariable(), is( alias ) );
		}
	}

	private void checkAttributeReferenceExpression(
			SqmQuerySpec querySpect,
			int attributeIndex,
			String typeName,
			String attributeName,
			String alias) {
		List<SqmSelection> selections = querySpect.getSelectClause().getSelections();
		SqmSelection selection = selections.get( attributeIndex );
		SqmSingularAttributeBinding expression = (SqmSingularAttributeBinding) selection.getExpression();
		assertThat( expression.getBoundNavigable().getAttributeName(), is( attributeName ) );
		if ( alias == null ) {
			assertTrue( ImplicitAliasGenerator.isImplicitAlias( selection.getAlias() ) );
		}
		else {
			assertThat( selection.getAlias(), is( alias ) );
		}
	}

	private void checkElementSelection(SqmQuerySpec querySpec, int selectionIndex, String typeName, String alias) {
		List<SqmSelection> selections = querySpec.getSelectClause().getSelections();
		SqmSelection selection = selections.get( selectionIndex );
		SqmExpression expression = selection.getExpression();
		EntityReference entityType = (EntityReference) expression.getExpressionType();
		assertThat( entityType.getTypeName(), is( typeName ) );
		if ( alias == null ) {
			assertTrue( ImplicitAliasGenerator.isImplicitAlias( selection.getAlias() ) );
		}
		else {
			assertThat( selection.getAlias(), is( alias ) );
		}
	}

	private void checkInSubqueryTestExpression(
			SqmQuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias
	) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		InSubQuerySqmPredicate predicate = (InSubQuerySqmPredicate) whereClause.getPredicate();
		SqmSingularAttributeBinding testExpression = (SqmSingularAttributeBinding) predicate.getTestExpression();
		assertThat( testExpression.getBoundNavigable().getAttributeName(), is( attributeName ) );
		assertThat( testExpression.getSourceBinding(), notNullValue() );
		assertThat(
				testExpression.getSourceBinding().getExportedFromElement().getIdentificationVariable(),
				is( alias )
		);
	}

	private void checkRelationalPredicateLeftHandWhereExpression(
			SqmQuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) whereClause.getPredicate();
		SqmSingularAttributeBinding leftHandExpression = (SqmSingularAttributeBinding) predicate.getLeftHandExpression();
		assertThat( leftHandExpression.getBoundNavigable().getAttributeName(), is( attributeName ) );
		assertThat(
				leftHandExpression.getSourceBinding().getExportedFromElement().getIdentificationVariable(),
				is( alias )
		);
	}

	private void checkRelationalPredicateRightHandWhereExpression(
			SqmQuerySpec querySpec,
			String typeName,
			String attributeName,
			String alias) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) whereClause.getPredicate();
		SqmSingularAttributeBinding leftHandExpression = (SqmSingularAttributeBinding) predicate.getRightHandExpression();
		assertThat( leftHandExpression.getBoundNavigable().getAttributeName(), is( attributeName ) );
		assertThat(
				leftHandExpression.getSourceBinding().getExportedFromElement().getIdentificationVariable(),
				is( alias )
		);
//		assertThat( leftHandExpression.getAttributeBinding().getExpressionType().getTypeName(), is( typeName ) );
	}

	private SubQuerySqmExpression getInSubQueryExpression(SqmSelectStatement selectStatement) {
		return getInSubQueryExpression( selectStatement.getQuerySpec() );
	}

	private SubQuerySqmExpression getInSubQueryExpression(SqmQuerySpec querySpec) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		InSubQuerySqmPredicate predicate = (InSubQuerySqmPredicate) whereClause.getPredicate();

		return predicate.getSubQueryExpression();
	}

	private SubQuerySqmExpression getLeftAndPredicateSubQueryExpression(SqmQuerySpec querySpec) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		AndSqmPredicate predicate = (AndSqmPredicate) whereClause.getPredicate();

		return ((InSubQuerySqmPredicate) predicate.getLeftHandPredicate()).getSubQueryExpression();
	}

	private SubQuerySqmExpression getRightAndPredicateSubQueryExpression(SqmQuerySpec querySpec) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		AndSqmPredicate predicate = (AndSqmPredicate) whereClause.getPredicate();

		return ((InSubQuerySqmPredicate) predicate.getRightHandPredicate()).getSubQueryExpression();
	}

	private SubQuerySqmExpression getRelationaSubQueryExpression(SqmQuerySpec querySpec) {
		SqmWhereClause whereClause = querySpec.getWhereClause();
		RelationalSqmPredicate predicate = (RelationalSqmPredicate) whereClause.getPredicate();
		return (SubQuerySqmExpression) predicate.getRightHandExpression();
	}

	private SqmSelectStatement interpretQuery(String query) {
		return (SqmSelectStatement) SemanticQueryProducer.interpret(
				query,
				consumerContext
		);
	}

	@javax.persistence.Entity( name = "Entity" )
	public static class Entity {
		@Id
		public Integer id;

		String basic;
		String basic1;
	}

	@javax.persistence.Entity( name = "Anything" )
	public static class Anything {
		@Id
		public Integer id;

		String address;
		String name;
		Long basic;
		Long basic1;
		Long basic2;
		Long basic3;
		Long b;
	}

	@javax.persistence.Entity( name = "Something" )
	public static class Something {
		@Id
		public Integer id;

		Long basic;
		Long basic1;
		Long basic2;
		Long basic3;
		Long basic4;

		@ManyToOne
		Entity entity;
	}

	@javax.persistence.Entity( name = "SomethingElse" )
	public static class SomethingElse {
		@Id
		public Integer id;

		Long basic;
		Long basic1;
		Long basic2;
		Long basic3;

		@ManyToOne
		Entity entity;
	}
}

