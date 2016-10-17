/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import java.util.List;
import java.util.Map;

import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.PluralAttributeReference.CollectionClassification;
import org.hibernate.sqm.domain.PluralAttributeReference.ElementReference.ElementClassification;
import org.hibernate.sqm.domain.PluralAttributeReference.IndexReference.IndexClassification;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.common.AttributeBinding;
import org.hibernate.sqm.parser.common.EntityBinding;
import org.hibernate.sqm.parser.common.MapKeyBinding;
import org.hibernate.sqm.parser.common.PluralAttributeElementBinding;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.MapEntrySqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationTarget;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.containsString;
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
public class SelectClauseTests {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl entity2Type = metamodel.makeEntityType( "com.acme.Entity2" );
		entity2Type.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);

		EntityTypeImpl entityType = metamodel.makeEntityType( "com.acme.Entity" );
		entityType.makeSingularAttribute(
				"basic",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic1",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.LONG
		);
		entityType.makeSingularAttribute(
				"basic2",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic3",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"basic4",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		entityType.makeSingularAttribute(
				"from",
				SingularAttributeClassification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl legType = metamodel.makeEntityType( "com.acme.Leg" );

		EntityTypeImpl tripType = metamodel.makeEntityType( "com.acme.Trip" );
		tripType.makeMapAttribute(
				"mapLegs",
				StandardBasicTypeDescriptors.INSTANCE.STRING,
				legType
		);
		tripType.makeListAttribute(
				"collectionLegs",
				StandardBasicTypeDescriptors.INSTANCE.INTEGER,
				legType
		);

		return metamodel;
	}

	@Test
	public void testSimpleAliasSelection() {
		SqmSelectStatement statement = interpret( "select o from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( EntityBinding.class ) );
	}

	@Test
	public void testSimpleAttributeSelection() {
		SqmSelectStatement statement = interpret( "select o.basic from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( AttributeBinding.class ) );
	}

	@Test
	public void testCompoundAttributeSelection() {
		SqmSelectStatement statement = interpret( "select o.basic1, o.basic2 from Entity o" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
	}

	@Test
	public void testMixedAliasAndAttributeSelection() {
		SqmSelectStatement statement = interpret( "select o, o.basic1 from Entity o" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( EntityBinding.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
	}

	@Test
	public void testSimpleDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpret( "select new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2) from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
	}

	@Test
	public void testMultipleDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpret(
				"select new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2), " +
						"new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2) " +
						"from Entity o"
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
		SqmSelectStatement statement = interpret(
				"select new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2), o.basic3 from Entity o"
		);
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( SqmDynamicInstantiation.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
	}

	@Test
	public void testNestedDynamicInstantiationSelection() {
		SqmSelectStatement statement = interpret(
				"select new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(" +
						"    o.basic1, " +
						"    o.basic2, " +
						"    new org.hibernate.test.sqm.parser.hql.SelectClauseTests$DTO(o.basic3, o.basic4) " +
						" ) " +
						"from Entity o"
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
				instanceOf( AttributeBinding.class )
		);
		assertThat(
				dynamicInstantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
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
		SqmSelectStatement statement = interpret( "select new list(o.basic1, o.basic2) from Entity o" );
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
				instanceOf( AttributeBinding.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
	}

	@Test
	public void testSimpleDynamicMapInstantiation() {
		SqmSelectStatement statement = interpret( "select new map(o.basic1 as a, o.basic2 as b) from Entity o" );
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
				instanceOf( AttributeBinding.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeBinding.class )
		);
	}

	@Test
	public void testBinaryArithmeticExpression() {
		final String query = "select o.basic + o.basic1 as b from Entity o";
		final SqmSelectStatement selectStatement = interpret( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		final SqmSelection selection = querySpec.getSelectClause().getSelections().get( 0 );

		assertThat( querySpec.getFromClause().getFromElementSpaces().size(), is(1) );
		final FromElementSpace fromElementSpace = querySpec.getFromClause().getFromElementSpaces().get( 0 );
		final SqmRoot root = fromElementSpace.getRoot();
		assertThat( root.getDomainReferenceBinding().getBoundDomainReference().getEntityName(), is( "com.acme.Entity" ) );
		assertThat( fromElementSpace.getJoins().size(), is(0) );

		BinaryArithmeticSqmExpression expression = (BinaryArithmeticSqmExpression) selection.getExpression();
		AttributeBinding leftHandOperand = (AttributeBinding) expression.getLeftHandOperand();
		assertThat( leftHandOperand.getLhs().getFromElement(), sameInstance( root ) );
		assertThat( leftHandOperand.getAttribute().getAttributeName(), is( "basic" ) );
		assertThat( leftHandOperand.getFromElement(), nullValue() );

		AttributeBinding rightHandOperand = (AttributeBinding) expression.getRightHandOperand();
		assertThat( rightHandOperand.getLhs().getFromElement(), sameInstance( root ) );
		assertThat( rightHandOperand.getAttribute().getAttributeName(), is( "basic1" ) );
		assertThat( leftHandOperand.getFromElement(), nullValue() );
	}

	@Test
	public void testBinaryArithmeticExpressionWithMultipleFromSpaces() {
		final String query = "select o.basic + a.basic1 as b from Entity o, Entity2 a";
		final SqmSelectStatement selectStatement = interpret( query );

		final SqmQuerySpec querySpec = selectStatement.getQuerySpec();
		final SqmSelection selection = querySpec.getSelectClause().getSelections().get( 0 );

		assertThat( querySpec.getFromClause().getFromElementSpaces().size(), is(2) );

		final SqmRoot entityRoot = querySpec.getFromClause().getFromElementSpaces().get( 0 ).getRoot();
		assertThat( entityRoot.getDomainReferenceBinding().getBoundDomainReference().getEntityName(), is( "com.acme.Entity" ) );

		final SqmRoot entity2Root = querySpec.getFromClause().getFromElementSpaces().get( 1 ).getRoot();
		assertThat( entity2Root.getDomainReferenceBinding().getBoundDomainReference().getEntityName(), is( "com.acme.Entity2" ) );

		BinaryArithmeticSqmExpression addExpression = (BinaryArithmeticSqmExpression) selection.getExpression();

		AttributeBinding leftHandOperand = (AttributeBinding) addExpression.getLeftHandOperand();
		assertThat( leftHandOperand.getLhs().getFromElement(), sameInstance( entityRoot ) );
		assertThat( leftHandOperand.getAttribute().getAttributeName(), is( "basic" ) );
		assertThat( leftHandOperand.getFromElement(), nullValue() );

		AttributeBinding rightHandOperand = (AttributeBinding) addExpression.getRightHandOperand();
		assertThat( rightHandOperand.getLhs().getFromElement(), sameInstance( entity2Root ) );
		assertThat( rightHandOperand.getAttribute().getAttributeName(), is( "basic1" ) );
		assertThat( rightHandOperand.getFromElement(), nullValue() );
	}

	@Test
	public void testMapKeyFunction() {
		SqmSelectStatement statement = interpret( "SELECT KEY( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( MapKeyBinding.class )
		);

		final MapKeyBinding mapKeyPathExpression = (MapKeyBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		final PluralAttributeReference attrRef = (PluralAttributeReference) mapKeyPathExpression.getPluralAttributeBinding().getAttribute();
		assertThat( attrRef.getCollectionClassification(), is(CollectionClassification.MAP) );
		assertThat( attrRef.getIndexReference().getClassification(), is( IndexClassification.BASIC) );
		assertThat( mapKeyPathExpression.getExpressionType(), sameInstance( attrRef.getIndexReference().getType() ) );

		assertThat( mapKeyPathExpression.getPluralAttributeBinding().getFromElement().getIdentificationVariable(), is( "l") );
	}

	@Test
	public void testMapValueFunction() {
		SqmSelectStatement statement = interpret( "SELECT VALUE( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( PluralAttributeElementBinding.class )
		);

		final PluralAttributeElementBinding elementBinding = (PluralAttributeElementBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		final PluralAttributeReference attrRef = elementBinding.getPluralAttributeReference();

		assertThat( attrRef.getCollectionClassification(), is(CollectionClassification.MAP) );
//		assertThat( elementBinding.getExpressionType(), sameInstance( attrRef.getElementReference().getType() ) );
		assertThat( attrRef.getElementReference().getClassification(), is( ElementClassification.ONE_TO_MANY) );
		assertThat( elementBinding.getPluralAttributeBinding().getFromElement().getIdentificationVariable(), is( "l") );
	}

	@Test
	public void testCollectionValueFunction() {
		SqmSelectStatement statement = interpret( "SELECT VALUE( l ) FROM Trip t JOIN t.collectionLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( PluralAttributeElementBinding.class )
		);

		final PluralAttributeElementBinding elementBinding = (PluralAttributeElementBinding) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		final PluralAttributeReference attrRef = elementBinding.getPluralAttributeReference();

		assertThat( attrRef.getCollectionClassification(), is(CollectionClassification.LIST) );
//		assertThat( elementBinding.getExpressionType(), sameInstance( attrRef.getElementReference().getType() ) );
		assertThat( attrRef.getElementReference().getClassification(), is( ElementClassification.ONE_TO_MANY) );
		assertThat( elementBinding.getPluralAttributeBinding().getFromElement().getIdentificationVariable(), is( "l") );
	}

	@Test
	public void testCollectionValueFunctionNotSupportedInStrictMode() {
		consumerContext.enableStrictJpaCompliance();

		expectedException.expect( StrictJpaComplianceViolation.class );
		expectedException.expectMessage( "Encountered application of value() function to path expression which does not resolve to a persistent Map" );

		interpret( "SELECT VALUE( l ) FROM Trip t JOIN t.collectionLegs l" );
	}

	@Test
	public void testMapEntryFunction() {
		SqmSelectStatement statement = interpret( "SELECT ENTRY( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( MapEntrySqmExpression.class )
		);

		final MapEntrySqmExpression mapEntryFunction = (MapEntrySqmExpression) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( mapEntryFunction.getAttributeBinding().getFromElement(), notNullValue() );
		assertThat( mapEntryFunction.getAttributeBinding().getFromElement().getIdentificationVariable(), is( "l") );

		final PluralAttributeReference attrRef = (PluralAttributeReference) mapEntryFunction.getAttributeBinding().getAttribute();
		assertThat( attrRef.getCollectionClassification(), is(CollectionClassification.MAP) );

		// Key
		assertThat( attrRef.getIndexReference().getClassification(), is( IndexClassification.BASIC) );
		assertThat( attrRef.getIndexReference().getType().asLoggableText(), containsString( String.class.getName() ) );

		// value/element
		assertThat( attrRef.getElementReference().getClassification(), is( ElementClassification.ONE_TO_MANY) );
		assertThat( ( (EntityReference) attrRef.getElementReference().getType() ).getEntityName(), is( "com.acme.Leg" ) );
	}

	private SqmSelectStatement interpret(String query) {
		return (SqmSelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}

	public static class DTO {
	}
}
