/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.function.ConcatFunctionSqmExpression;
import org.hibernate.query.sqm.tree.expression.function.SubstringFunctionSqmExpression;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.query.sqm.produce.spi.SemanticQueryProducer.interpret;

/**
 * @author Steve Ebersole
 */
public class NestedFunctionsTest extends StandardModelTest {
	@Test
	public void testSubstrInsideConcat() {
		final SqmSelectStatement statement = interpretSelect( "select concat('111', substring('222222', 1, 3)) from Person" );
		assertThat( statement.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( ConcatFunctionSqmExpression.class ) );
		final ConcatFunctionSqmExpression concatFunction = (ConcatFunctionSqmExpression) selection.getExpression();
		assertThat( concatFunction.getExpressions().size(), is(2) );
		// check that the second expression/argument is the substr function
		assertThat( concatFunction.getExpressions().get( 1 ), instanceOf( SubstringFunctionSqmExpression.class ) );
	}
}
