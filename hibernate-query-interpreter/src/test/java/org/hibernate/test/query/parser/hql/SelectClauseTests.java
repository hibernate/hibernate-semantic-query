/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.MapEntryFunction;
import org.hibernate.sqm.query.expression.MapKeyFunction;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.DynamicInstantiationTarget;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
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

	private ConsumerContextImpl consumerContext;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUpContext() {
		 consumerContext = new ConsumerContextImpl();
	}

	@Test
	public void testSimpleAliasSelection() {
		SelectStatement statement = interpret( "select o from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		Selection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( FromElementReferenceExpression.class ) );
	}

	@Test
	public void testSimpleAttributeSelection() {
		SelectStatement statement = interpret( "select o.basic from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		Selection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( AttributeReferenceExpression.class ) );
	}

	@Test
	public void testCompoundAttributeSelection() {
		SelectStatement statement = interpret( "select o.basic1, o.basic2 from Entity o" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
	}

	@Test
	public void testMixedAliasAndAttributeSelection() {
		SelectStatement statement = interpret( "select o, o.basic1 from Entity o" );
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( FromElementReferenceExpression.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
	}

	@Test
	public void testSimpleDynamicInstantiationSelection() {
		SelectStatement statement = interpret( "select new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2) from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
	}

	@Test
	public void testMultipleDynamicInstantiationSelection() {
		SelectStatement statement = interpret(
				"select new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2), " +
						"new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2) " +
						"from Entity o"
		);
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
	}

	@Test
	public void testMixedAttributeAndDynamicInstantiationSelection() {
		SelectStatement statement = interpret(
				"select new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(o.basic1, o.basic2), o.basic3 from Entity o"
		);
		assertEquals( 2, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
	}

	@Test
	public void testNestedDynamicInstantiationSelection() {
		SelectStatement statement = interpret(
				"select new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(" +
						"    o.basic1, " +
						"    o.basic2, " +
						"    new org.hibernate.test.query.parser.hql.SelectClauseTests$DTO(o.basic3, o.basic4) " +
						" ) " +
						"from Entity o"
		);
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);

		DynamicInstantiation dynamicInstantiation = (DynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				dynamicInstantiation.getInstantiationTarget().getNature(),
				equalTo( DynamicInstantiationTarget.Nature.CLASS )
		);
		assertThat(
				dynamicInstantiation.getInstantiationTarget().getTargetJavaType().getTypeName(),
				equalTo( DTO.class.getName() )
		);

		assertEquals( 3, dynamicInstantiation.getArguments().size() );
		assertThat(
				dynamicInstantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
		assertThat(
				dynamicInstantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
		assertThat(
				dynamicInstantiation.getArguments().get( 2 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
		DynamicInstantiation nestedInstantiation = (DynamicInstantiation) dynamicInstantiation.getArguments().get( 2 ).getExpression();
		assertThat(
				nestedInstantiation.getInstantiationTarget().getNature(),
				equalTo( DynamicInstantiationTarget.Nature.CLASS )
		);
		assertThat(
				nestedInstantiation.getInstantiationTarget().getTargetJavaType().getTypeName(),
				equalTo( DTO.class.getName() )
		);

	}

	@Test
	public void testSimpleDynamicListInstantiation() {
		SelectStatement statement = interpret( "select new list(o.basic1, o.basic2) from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
		DynamicInstantiation instantiation = (DynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				instantiation.getInstantiationTarget().getNature(),
				equalTo( DynamicInstantiationTarget.Nature.LIST )
		);
		assertThat(
				instantiation.getInstantiationTarget().getTargetJavaType(),
				CoreMatchers.<TypeDescriptor>sameInstance( StandardBasicTypeDescriptors.INSTANCE.LIST )
		);

		assertEquals( 2, instantiation.getArguments().size() );
		assertThat(
				instantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
	}

	@Test
	public void testSimpleDynamicMapInstantiation() {
		SelectStatement statement = interpret( "select new map(o.basic1 as a, o.basic2 as b) from Entity o" );
		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( DynamicInstantiation.class )
		);
		DynamicInstantiation instantiation = (DynamicInstantiation) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat(
				instantiation.getInstantiationTarget().getNature(),
				equalTo( DynamicInstantiationTarget.Nature.MAP )
		);
		assertThat(
				instantiation.getInstantiationTarget().getTargetJavaType(),
				CoreMatchers.<TypeDescriptor>sameInstance( StandardBasicTypeDescriptors.INSTANCE.MAP )
		);

		assertEquals( 2, instantiation.getArguments().size() );
		assertThat(
				instantiation.getArguments().get( 0 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
		assertThat(
				instantiation.getArguments().get( 1 ).getExpression(),
				instanceOf( AttributeReferenceExpression.class )
		);
	}

	@Test
	public void testBinaryArithmeticExpression() {
		final String query = "select o.basic + o.basic1 as b from Entity o";
		final SelectStatement selectStatement = interpret( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();
		final Selection selection = querySpec.getSelectClause().getSelections().get( 0 );
		BinaryArithmeticExpression expression = (BinaryArithmeticExpression) selection.getExpression();
		AttributeReferenceExpression leftHandOperand = (AttributeReferenceExpression) expression.getLeftHandOperand();
		assertThat( leftHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( leftHandOperand.getAttributeDescriptor().getName(), is( "basic" ) );

		AttributeReferenceExpression rightHandOperand = (AttributeReferenceExpression) expression.getRightHandOperand();
		assertThat( rightHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( rightHandOperand.getAttributeDescriptor().getName(), is( "basic1" ) );
	}

	@Test
	public void testBinaryArithmeticExpressionWithMultipleFromSpaces() {
		final String query = "select o.basic + a.basic1 as b from Entity o, Entity2 a";
		final SelectStatement selectStatement = interpret( query );

		final QuerySpec querySpec = selectStatement.getQuerySpec();
		final Selection selection = querySpec.getSelectClause().getSelections().get( 0 );
		BinaryArithmeticExpression expression = (BinaryArithmeticExpression) selection.getExpression();
		AttributeReferenceExpression leftHandOperand = (AttributeReferenceExpression) expression.getLeftHandOperand();
		assertThat( leftHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity" ) );
		assertThat( leftHandOperand.getAttributeDescriptor().getName(), is( "basic" ) );

		AttributeReferenceExpression rightHandOperand = (AttributeReferenceExpression) expression.getRightHandOperand();
		assertThat( rightHandOperand.getSource().getTypeDescriptor().getTypeName(), is( "com.acme.Entity2" ) );
		assertThat( rightHandOperand.getAttributeDescriptor().getName(), is( "basic1" ) );
	}

	@Test
	public void testMapKeyFunction() {
		SelectStatement statement = interpret( "SELECT KEY( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( MapKeyFunction.class )
		);

		MapKeyFunction mapKeyFunction = (MapKeyFunction) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertEquals("com.acme.map-key:mapLegs", mapKeyFunction.getMapKeyType().getTypeName() );
		assertEquals("l", mapKeyFunction.getCollectionAlias() );
	}

	@Test
	public void testMapValueFunction() {
		SelectStatement statement = interpret( "SELECT VALUE( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( CollectionValueFunction.class )
		);

		CollectionValueFunction collectionValueFunction = (CollectionValueFunction) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertEquals("com.acme.map-value:mapLegs", collectionValueFunction.getValueType().getTypeName() );
		assertEquals("l", collectionValueFunction.getCollectionAlias() );
	}

	@Test
	public void testCollectionValueFunction() {
		SelectStatement statement = interpret( "SELECT VALUE( l ) FROM Trip t JOIN t.collectionLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( CollectionValueFunction.class )
		);

		CollectionValueFunction collectionValueFunction = (CollectionValueFunction) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertEquals("com.acme.collection-value:collectionLegs", collectionValueFunction.getValueType().getTypeName() );
		assertEquals("l", collectionValueFunction.getCollectionAlias() );
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
		SelectStatement statement = interpret( "SELECT ENTRY( l ) FROM Trip t JOIN t.mapLegs l" );

		assertEquals( 1, statement.getQuerySpec().getSelectClause().getSelections().size() );
		assertThat(
				statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression(),
				instanceOf( MapEntryFunction.class )
		);

		MapEntryFunction mapEntryFunction = (MapEntryFunction) statement.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertEquals("com.acme.map-key:mapLegs", mapEntryFunction.getMapKeyType().getTypeName() );
		assertEquals("com.acme.map-value:mapLegs", mapEntryFunction.getMapValueType().getTypeName() );
		assertEquals("l", mapEntryFunction.getCollectionAlias() );
	}

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}

	public static class DTO {
	}
}
