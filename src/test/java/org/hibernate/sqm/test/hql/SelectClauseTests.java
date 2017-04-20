/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import java.util.List;
import java.util.Map;

import org.hibernate.query.sqm.StrictJpaComplianceViolation;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.SqmPluralAttribute;
import org.hibernate.query.sqm.domain.SqmPluralAttribute.CollectionClassification;
import org.hibernate.query.sqm.domain.SqmPluralAttributeElement.ElementClassification;
import org.hibernate.query.sqm.domain.SqmPluralAttributeIndex.IndexClassification;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.BinaryArithmeticSqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.AbstractSqmCollectionIndexBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmCollectionElementBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmMapEntryBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeBinding;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiation;
import org.hibernate.query.sqm.tree.select.SqmDynamicInstantiationTarget;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sqm.test.domain.EntityOfMaps;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.hibernate.test.sqm.domain.FromElementHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test various forms of selections
 *
 * @author Steve Ebersole
 */
public class SelectClauseTests extends StandardModelTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testSimpleAliasSelection() {
		SqmSelectStatement statement = interpretSelect( "select p from Person p" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmEntityBinding.class ) );
	}

	@Test
	public void testSimpleAttributeSelection() {
		SqmSelectStatement statement = interpretSelect( "select p.nickName from Person p" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( SqmSingularAttributeBinding.class ) );
	}

	@Test
	public void testCompoundAttributeSelection() {
		SqmSelectStatement statement = interpretSelect( "select p.nickName, p.name.first from Person p" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
	}

	@Test
	public void testMixedAliasAndAttributeSelection() {
		SqmSelectStatement statement = interpretSelect( "select p, p.nickName from Person p" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmEntityBinding.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
	}

	@Test
	public void testSimpleDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpretSelect(
				"select new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(p.nickName, p.numberOfToes) from Person p"
		);
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
	}

	@Test
	public void testMultipleDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpretSelect(
				"select new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(p.nickName, p.numberOfToes), " +
						"new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(p.nickName, p.numberOfToes) " +
						"from Person p"
		);
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
	}

	@Test
	public void testMixedAttributeAndDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpretSelect(
				"select new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(p.nickName, p.numberOfToes), p.nickName from Person p"
		);
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
	}

	@Test
	public void testNestedDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpretSelect(
				"select new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(" +
						"    p.nickName, " +
						"    p.numberOfToes, " +
						"    new org.hibernate.sqm.test.hql.SelectClauseTests$DTO(p.nickName, p.numberOfToes) " +
						" ) " +
						"from Person p"
		);
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);

		SqmDynamicInstantiation dynamicInstantiation = (SqmDynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				dynamicInstantiation.getInstantiationTarget().getNature(),
				equalTo( SqmDynamicInstantiationTarget.Nature.CLASS )
		);
		assertThat(
				dynamicInstantiation.getInstantiationTarget().getJavaType(),
				CoreMatchers.<Class>equalTo( DTO.class )
		);

		assertEquals( 3, dynamicInstantiation.getArguments().size() );
		assertThat(
				dynamicInstantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
		assertThat(
				dynamicInstantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
		assertThat(
				dynamicInstantiation.getArguments().get( 2 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		SqmDynamicInstantiation nestedInstantiation = (SqmDynamicInstantiation) dynamicInstantiation.getArguments().get( 2 ).getExpression();
		assertThat(
				nestedInstantiation.getInstantiationTarget().getNature(),
				equalTo( SqmDynamicInstantiationTarget.Nature.CLASS )
		);
		assertThat(
				nestedInstantiation.getInstantiationTarget().getJavaType(),
				CoreMatchers.<Class>equalTo( DTO.class )
		);

	}

	@Test
	public void testSimpleDynamicListInstantiation() {
		SqmSelectStatement statement = interpretSelect( "select new list(p.nickName, p.numberOfToes) from Person p" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		SqmDynamicInstantiation instantiation = (SqmDynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				instantiation.getInstantiationTarget().getNature(),
				equalTo( SqmDynamicInstantiationTarget.Nature.LIST )
		);
		assertThat(
				instantiation.getInstantiationTarget().getJavaType(),
				CoreMatchers.<Class>equalTo( List.class )
		);

		assertEquals( 2, instantiation.getArguments().size() );
		assertThat(
				instantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
	}

	@Test
	public void testSimpleDynamicMapInstantiation() {
		SqmSelectStatement statement = interpretSelect( "select new map(p.nickName as nn, p.numberOfToes as nt) from Person p" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		SqmDynamicInstantiation instantiation = (SqmDynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				instantiation.getInstantiationTarget().getNature(),
				equalTo( SqmDynamicInstantiationTarget.Nature.MAP )
		);
		assertThat(
				instantiation.getInstantiationTarget().getJavaType(),
				CoreMatchers.<Class>equalTo( Map.class )
		);

		assertEquals( 2, instantiation.getArguments().size() );
		assertThat(
				instantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( SqmSingularAttributeBinding.class )
		);
	}

	@Test
	public void testBinaryArithmeticExpression() {
		final String query = "select p.numberOfToes + p.numberOfToes as b from Person p";
		final SqmSelectStatement selectStatement = interpretSelect( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		final SqmSelection selection = querySpec.getSelectClause().getSelections().get( 0 );

		assertThat( querySpec.getFromClause().getFromElementSpaces().size(), is(1) );
		final SqmFromElementSpace fromElementSpace = querySpec.getFromClause().getFromElementSpaces().get( 0 );
		final SqmRoot root = fromElementSpace.getRoot();
		assertThat( root.getBinding().getBoundNavigable().getEntityName(), endsWith( "Person" ) );
		assertThat( fromElementSpace.getJoins().size(), is(0) );

		BinaryArithmeticSqmExpression expression = (BinaryArithmeticSqmExpression) selection.getExpression();
		SqmSingularAttributeBinding leftHandOperand = (SqmSingularAttributeBinding) expression.getLeftHandOperand();
		assertThat( leftHandOperand.getSourceBinding().getExportedFromElement(), sameInstance( root ) );
		assertThat( leftHandOperand.getBoundNavigable().getAttributeName(), is( "numberOfToes" ) );
//		assertThat( leftHandOperand.getFromElement(), nullValue() );

		SqmSingularAttributeBinding rightHandOperand = (SqmSingularAttributeBinding) expression.getRightHandOperand();
		assertThat( rightHandOperand.getSourceBinding().getExportedFromElement(), sameInstance( root ) );
		assertThat( rightHandOperand.getBoundNavigable().getAttributeName(), is( "numberOfToes" ) );
//		assertThat( leftHandOperand.getFromElement(), nullValue() );
	}

	@Test
	public void testBinaryArithmeticExpressionWithMultipleFromSpaces() {
		final String query = "select p.numberOfToes + p2.numberOfToes as b from Person p, Person p2";
		final SqmSelectStatement selectStatement = interpretSelect( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		final SqmSelection selection = querySpec.getSelectClause().getSelections().get( 0 );

		assertThat( querySpec.getFromClause().getFromElementSpaces().size(), is(2) );

		final SqmRoot entityRoot = querySpec.getFromClause().getFromElementSpaces().get( 0 ).getRoot();
		assertThat( entityRoot.getBinding().getBoundNavigable().getEntityName(), endsWith( "Person" ) );

		final SqmRoot entity2Root = querySpec.getFromClause().getFromElementSpaces().get( 1 ).getRoot();
		assertThat( entity2Root.getBinding().getBoundNavigable().getEntityName(), endsWith( "Person" ) );

		BinaryArithmeticSqmExpression addExpression = (BinaryArithmeticSqmExpression) selection.getExpression();

		SqmSingularAttributeBinding leftHandOperand = (SqmSingularAttributeBinding) addExpression.getLeftHandOperand();
		assertThat( leftHandOperand.getSourceBinding().getExportedFromElement(), sameInstance( entityRoot ) );
		assertThat( leftHandOperand.getBoundNavigable().getAttributeName(), is( "numberOfToes" ) );
		assertThat( FromElementHelper.extractExpressionExportedFromElement( leftHandOperand ), nullValue() );

		SqmSingularAttributeBinding rightHandOperand = (SqmSingularAttributeBinding) addExpression.getRightHandOperand();
		assertThat( rightHandOperand.getSourceBinding().getExportedFromElement(), sameInstance( entity2Root ) );
		assertThat( rightHandOperand.getBoundNavigable().getAttributeName(), is( "numberOfToes" ) );
		assertThat( FromElementHelper.extractExpressionExportedFromElement( rightHandOperand ), nullValue() );
	}

	@Test
	public void testMapKeyFunction() {
		collectionIndexFunctionAssertions(
				interpretSelect( "select key(m) from EntityOfMaps e join e.basicToBasicMap m" ),
				CollectionClassification.MAP,
				IndexClassification.BASIC,
				"m"
		);
		collectionIndexFunctionAssertions(
				interpretSelect( "select key(m) from EntityOfMaps e join e.componentToBasicMap m" ),
				CollectionClassification.MAP,
				IndexClassification.EMBEDDABLE,
				"m"
		);
	}

	private void collectionIndexFunctionAssertions(
			SqmSelectStatement statement,
			CollectionClassification collectionClassification,
			IndexClassification indexClassification,
			String collectionAlias) {
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( AbstractSqmCollectionIndexBinding.class )
		);

		final AbstractSqmCollectionIndexBinding mapKeyPathExpression = (AbstractSqmCollectionIndexBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		final SqmPluralAttribute attrRef = mapKeyPathExpression.getPluralAttributeBinding().getBoundNavigable();
		assertThat( attrRef.getCollectionClassification(), is(collectionClassification) );
		assertThat( attrRef.getIndexReference().getClassification(), is(indexClassification) );
		assertThat( mapKeyPathExpression.getExpressionType(), sameInstance( attrRef.getIndexReference() ) );

		assertThat(
				FromElementHelper.extractExpressionExportedFromElement( mapKeyPathExpression.getPluralAttributeBinding() ).getIdentificationVariable(),
				is(collectionAlias)
		);
	}

	@Test
	public void testMapValueFunction() {
		collectionValueFunctionAssertions(
				interpretSelect( "select value(m) from EntityOfMaps e join e.basicToBasicMap m" ),
				CollectionClassification.MAP,
				ElementClassification.BASIC,
				"m"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(m) from EntityOfMaps e join e.basicToComponentMap m" ),
				CollectionClassification.MAP,
				ElementClassification.EMBEDDABLE,
				"m"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(m) from EntityOfMaps e join e.basicToOneToMany m" ),
				CollectionClassification.MAP,
				ElementClassification.ONE_TO_MANY,
				"m"
		);
	}

	@Test
	public void testCollectionValueFunction() {
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfLists e join e.listOfBasics b" ),
				CollectionClassification.LIST,
				ElementClassification.BASIC,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfLists e join e.listOfComponents b" ),
				CollectionClassification.LIST,
				ElementClassification.EMBEDDABLE,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfLists e join e.listOfOneToMany b" ),
				CollectionClassification.LIST,
				ElementClassification.ONE_TO_MANY,
				"b"
		);
		// todo : ManyToMany not properly handled atm

		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfSets e join e.setOfBasics b" ),
				CollectionClassification.SET,
				ElementClassification.BASIC,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfSets e join e.setOfComponents b" ),
				CollectionClassification.SET,
				ElementClassification.EMBEDDABLE,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfSets e join e.setOfOneToMany b" ),
				CollectionClassification.SET,
				ElementClassification.ONE_TO_MANY,
				"b"
		);
		// todo : ManyToMany not properly handled atm

		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfMaps e join e.basicToBasicMap b" ),
				CollectionClassification.MAP,
				ElementClassification.BASIC,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfMaps e join e.basicToComponentMap b" ),
				CollectionClassification.MAP,
				ElementClassification.EMBEDDABLE,
				"b"
		);
		collectionValueFunctionAssertions(
				interpretSelect( "select value(b) from EntityOfMaps e join e.basicToOneToMany b" ),
				CollectionClassification.MAP,
				ElementClassification.ONE_TO_MANY,
				"b"
		);
		// todo : ManyToMany not properly handled atm
	}

	private void collectionValueFunctionAssertions(
			SqmSelectStatement statement,
			CollectionClassification collectionClassification,
			ElementClassification elementClassification,
			String collectionIdentificationVariable) {
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmCollectionElementBinding.class )
		);

		final SqmCollectionElementBinding elementBinding = (SqmCollectionElementBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		final SqmPluralAttributeBinding attrRef = elementBinding.getSourceBinding();

		assertThat( attrRef.getBoundNavigable().getCollectionClassification(), is(collectionClassification) );
//		assertThat( elementBinding.getExpressionType(), sameInstance( attrRef.getElementReference().getType() ) );
		assertThat( attrRef.getBoundNavigable().getElementReference().getClassification(), is( elementClassification ) );
		assertThat( attrRef.getExportedFromElement().getIdentificationVariable(), is( collectionIdentificationVariable ) );
	}

	@Test
	public void testCollectionValueFunctionNotSupportedInStrictMode() {
		consumerContext.enableStrictJpaCompliance();

		expectedException.expect( StrictJpaComplianceViolation.class );
		expectedException.expectMessage( "Encountered application of value() function to path expression which does not resolve to a persistent Map" );

		interpretSelect( "select value(b) from EntityOfLists e join e.listOfBasics b" );
	}

	@Test
	public void testMapEntryFunction() {
		SqmSelectStatement statement = interpretSelect( "select entry(m) from EntityOfMaps e join e.basicToManyToMany m" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmMapEntryBinding.class )
		);

		final SqmMapEntryBinding mapEntryFunction = (SqmMapEntryBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( mapEntryFunction.getAttributeBinding().getExportedFromElement(), notNullValue() );
		assertThat( mapEntryFunction.getAttributeBinding().getExportedFromElement().getIdentificationVariable(), is( "m") );

		final SqmPluralAttribute attrRef = mapEntryFunction.getAttributeBinding().getBoundNavigable();
		assertThat( attrRef.getCollectionClassification(), is(CollectionClassification.MAP) );

		// Key
		assertThat( attrRef.getIndexReference().getClassification(), is( IndexClassification.BASIC) );
		assertEquals( String.class, attrRef.getIndexReference().getExportedDomainType().getJavaType() );

		// value/element
		assertThat( attrRef.getElementReference().getClassification(), is( ElementClassification.ONE_TO_MANY) );
		assertThat( ( (SqmExpressableTypeEntity) attrRef.getElementReference() ).getEntityName(), is( "org.hibernate.sqm.test.domain.EntityOfMaps" ) );
		assertEquals( EntityOfMaps.class, attrRef.getElementReference().getExportedDomainType().getJavaType() );
	}

	public static class DTO {
	}
}
