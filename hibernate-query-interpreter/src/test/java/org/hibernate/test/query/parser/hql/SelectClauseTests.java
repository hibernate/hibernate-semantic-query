/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.query.parser.StrictJpaComplianceViolation;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.CollectionValueFunction;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.MapKeyFunction;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
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

	private SelectStatement interpret(String query) {
		return (SelectStatement) SemanticQueryInterpreter.interpret( query, consumerContext );
	}

	public static class DTO {
	}
}
