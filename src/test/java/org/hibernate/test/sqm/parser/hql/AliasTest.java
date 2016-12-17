/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import java.util.List;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.AliasCollisionException;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.parser.common.ImplicitAliasGenerator;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.SubQuerySqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrea Boriero
 */
public class AliasTest {

	final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

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

		checkElementSelection( querySpec, 0, "com.acme.Entity", null );
		checkAttributeReferenceExpression( querySpec, 1, "com.acme.Entity", "basic1", "o1" );

		checkFromClause( querySpec, 0, "com.acme.Entity", "o" );
	}

	@Test
	public void testDefiningDifferentResultVAriables() {
		final String query = "select a.basic as b, a.basic2 as c from Anything a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkAttributeReferenceExpression( querySpec, 0, "com.acme.Anything", "basic", "b" );
		checkAttributeReferenceExpression( querySpec, 1, "com.acme.Anything", "basic2", "c" );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
	}

	@Test
	public void testDefiningAResultVariableEqualsToAnIdentificationVariable() {
		final String query = "select a as a from Anything as a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

		checkElementSelection( querySpec, 0, "com.acme.Anything", "a" );

		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
	}

	@Test
	public void testReusingAnIdentificationVariableInSelectClause() {
		final String query = "select a.basic from Anything as a";
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

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
		final SqmQuerySpec querySpec = interpretQuery( query ).getQuerySpec();

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
		final SqmSelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, "com.acme.Anything", null );
		checkFromClause( selectStatement.getQuerySpec(), 0, "com.acme.Anything", "a" );

		final SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, "com.acme.SomethingElse", null );
		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "b" );
		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic", "b" );
	}

	@Test
	public void testSameIdentificationVariablesInSubquery() {
		final String query = "select a from Anything a where a.basic1 in ( select a from SomethingElse a where a.basic = 5)";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );

		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic1", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

		checkElementSelection( subQuerySpec, 0, "com.acme.SomethingElse", null );

		checkFromClause( subQuerySpec, 0, "com.acme.SomethingElse", "a" );

		checkRelationalPredicateLeftHandWhereExpression( subQuerySpec, "com.acme.SomethingElse", "basic", "a" );
	}

	@Test
	public void testSubqueryUsingIdentificationVariableDefinedInRootQuery() {
		final String query = "select a from Anything a where a.basic in " +
				"( select b.basic from SomethingElse b where a.basic = b.basic2 )";
		final SqmSelectStatement selectStatement = interpretQuery( query );

		checkElementSelection( selectStatement.getQuerySpec(), 0, "com.acme.Anything", null );
		checkFromClause( selectStatement.getQuerySpec(), 0, "com.acme.Anything", "a" );

		checkInSubqueryTestExpression( selectStatement.getQuerySpec(), "com.acme.Anything", "basic", "a" );
		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();
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
		final SqmSelectStatement selectStatement = interpretQuery( query );

		SqmQuerySpec subQuerySpec = getLeftAndPredicateSubQueryExpression( selectStatement.getQuerySpec() ).getQuerySpec();
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
		final SqmSelectStatement selectStatement = interpretQuery( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

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
		final SqmSelectStatement selectStatement = interpretQuery( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		checkElementSelection( querySpec, 0, "com.acme.Anything", null );
		checkFromClause( querySpec, 0, "com.acme.Anything", "a" );
		checkInSubqueryTestExpression( querySpec, "com.acme.Anything", "basic", "a" );

		SqmQuerySpec subQuerySpec = getInSubQueryExpression( selectStatement ).getQuerySpec();

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

	@Test
	public void testIdentificationVariableReferencedInWhewClause() {
		final String query = "select a.address as c from Anything a where c = '2'";
		interpretQuery( query );
	}

	private void checkFromClause(SqmQuerySpec querySpec, int fromClauseIndex, String typeName, String alias) {
		SqmFromClause fromClause = querySpec.getFromClause();
		FromElementSpace fromElementSpace = fromClause.getFromElementSpaces().get( fromClauseIndex );
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
		SingularAttributeBinding expression = (SingularAttributeBinding) selection.getExpression();
		assertThat( expression.getAttribute().getAttributeName(), is( attributeName ) );
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
		EntityTypeImpl entityType = (EntityTypeImpl) expression.getExpressionType();
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
		SingularAttributeBinding testExpression = (SingularAttributeBinding) predicate.getTestExpression();
		assertThat( testExpression.getAttribute().getAttributeName(), is( attributeName ) );
		assertThat(
				testExpression.getLhs().getFromElement().getIdentificationVariable(),
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
		SingularAttributeBinding leftHandExpression = (SingularAttributeBinding) predicate.getLeftHandExpression();
		assertThat( leftHandExpression.getAttribute().getAttributeName(), is( attributeName ) );
		assertThat(
				leftHandExpression.getLhs().getFromElement().getIdentificationVariable(),
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
		SingularAttributeBinding leftHandExpression = (SingularAttributeBinding) predicate.getRightHandExpression();
		assertThat( leftHandExpression.getAttribute().getAttributeName(), is( attributeName ) );
		assertThat(
				leftHandExpression.getLhs().getFromElement().getIdentificationVariable(),
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
		return (SqmSelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl anythingType = metamodel.makeEntityType( "com.acme.Anything" );
		anythingType.makeSingularAttribute(
				"address",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		anythingType.makeSingularAttribute(
				"name",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		anythingType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		anythingType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		anythingType.makeSingularAttribute(
				"basic2",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		anythingType.makeSingularAttribute(
				"b",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		EntityTypeImpl somethingType = metamodel.makeEntityType( "com.acme.Something" );
		somethingType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"basic3",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingType.makeSingularAttribute(
				"entity",
				SingularAttributeClassification.BASIC,
				entityType
		);

		EntityTypeImpl somethingElseType = metamodel.makeEntityType( "com.acme.SomethingElse" );
		somethingElseType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"basic2",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		somethingElseType.makeSingularAttribute(
				"basic3",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		return metamodel;
	}
}

