/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.query.sqm.produce.spi.ImplicitAliasGenerator;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmFromClause;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Initial work on a "from clause processor"
 *
 * @author Steve Ebersole
 */
public class FromClauseTests extends StandardModelTest {
	@Test
	public void testSimpleFrom() throws Exception {
		final SqmSelectStatement selectStatement = interpretSelect( "select p.nickName from Person p" );

		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		SqmRoot root = space1.getRoot();
		assertNotNull( root );
		assertThat( root.getIdentificationVariable(), is( "p") );
	}

	@Test
	public void testMultipleSpaces() throws Exception {
		final SqmSelectStatement selectStatement = interpretSelect( "select p.nickName from Person p, Person p2" );

		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
//		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 2, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		SqmFromElementSpace space2 = fromClause1.getFromElementSpaces().get( 1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		assertNotNull( space2.getRoot() );
		assertEquals( 0, space2.getJoins().size() );

		assertNotNull( space1.getRoot() );
		assertThat( space1.getRoot().getIdentificationVariable(), is( "p")  );

		assertNotNull( space2.getRoot() );
		assertThat( space2.getRoot().getIdentificationVariable(), is( "p2")  );
	}

	@Test
	public void testImplicitAlias() throws Exception {
		final SqmSelectStatement selectStatement = interpretSelect( "select nickName from Person" );

		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
//		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		assertTrue( ImplicitAliasGenerator.isImplicitAlias( space1.getRoot().getIdentificationVariable() ) );
	}

	@Test
	public void testCrossJoin() throws Exception {
		final SqmSelectStatement selectStatement = interpretSelect( "select p.nickName from Person p cross join Person p2" );

		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
//		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 1, space1.getJoins().size() );
	}

	@Test
	public void testSimpleImplicitInnerJoin() throws Exception {
		simpleJoinAssertions(
				interpretSelect( "select p.nickName from Person p join p.mate m" ),
				SqmJoinType.INNER
		);
	}

	private void simpleJoinAssertions(SqmSelectStatement selectStatement, SqmJoinType joinType) {
		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
//		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 1, space1.getJoins().size() );
		assertEquals( joinType, space1.getJoins().get( 0 ).getJoinType() );
	}

	@Test
	public void testSimpleExplicitInnerJoin() throws Exception {
		simpleJoinAssertions(
				interpretSelect( "select a.nickName from Person a inner join a.mate c" ),
				SqmJoinType.INNER
		);
	}

	@Test
	public void testSimpleExplicitOuterJoin() throws Exception {
		simpleJoinAssertions(
				interpretSelect( "select a.nickName from Person a outer join a.mate c" ),
				SqmJoinType.LEFT
		);
	}

	@Test
	public void testSimpleExplicitLeftOuterJoin() throws Exception {
		simpleJoinAssertions(
				interpretSelect( "select a.nickName from Person a left outer join a.mate c" ),
				SqmJoinType.LEFT
		);
	}

	@Test
	public void testAttributeJoinWithOnClause() throws Exception {
		SqmSelectStatement selectStatement = interpretSelect( "select a from Person a left outer join a.mate c on c.numberOfToes > 5 and c.numberOfToes < 20 " );

		final SqmFromClause fromClause1 = selectStatement.getQuerySpec().getFromClause();
		assertNotNull( fromClause1 );
//		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		SqmFromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 1, space1.getJoins().size() );
	}
}
