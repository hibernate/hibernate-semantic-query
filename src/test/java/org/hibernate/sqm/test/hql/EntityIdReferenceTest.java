/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.test.hql;

import org.hibernate.orm.persister.common.spi.SingularAttribute;
import org.hibernate.orm.persister.entity.spi.IdentifierDescriptorSimple;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.expression.domain.SqmEntityIdentifierBindingBasic;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBinding;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * The Hibernate ORM persisters keep id descriptions specially.  Specifically they are
 * not part of the grouping of "normal attributes".  One ramification of this special
 * treatment of 'id' references in sqm.  These tests assert that we handle those
 * ramifications correctly.
 *
 * @author Steve Ebersole
 */
public class EntityIdReferenceTest extends StandardModelTest {
	@Test
	public void testReferenceSimpleIdAttributeNamedId() {
		SqmSelectStatement sqm = interpretSelect( "select p.id from Person p" );
		SqmEntityIdentifierBindingBasic idBinding = (SqmEntityIdentifierBindingBasic) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
//		assertThat( idReference.getExpressionType().getTypeName(), is(Integer.class.getName() ) );

		sqm = (SqmSelectStatement) interpret( "select p.pk from Person p" );
		SqmEntityIdentifierBindingBasic pkReference = (SqmEntityIdentifierBindingBasic) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
//		assertThat( pkReference.getExpressionType().getTypeName(), is(Integer.class.getName() ) );

		assertThat( idBinding.getBoundNavigable(), sameInstance( pkReference.getBoundNavigable() ) );
	}

	@Test
	public void testEntityWithNonIdAttributeNamedId() {
		SqmSelectStatement sqm = interpretSelect( "select p.id from EntityWithNonIdAttributeNamedId p" );
		SqmSingularAttributeBinding idReference = (SqmSingularAttributeBinding) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		SingularAttribute idOrmSingularAttribute = (SingularAttribute) idReference.getBoundNavigable();
		assertFalse( idOrmSingularAttribute.isId() );
//		assertThat( idReference.getExpressionType().getTypeName(), is( String.class.getName() ) );

		sqm = interpretSelect( "select p.pk from EntityWithNonIdAttributeNamedId p" );
		SqmEntityIdentifierBindingBasic pkReference = (SqmEntityIdentifierBindingBasic) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		SingularAttribute pkOrmSingularAttribute = ( (IdentifierDescriptorSimple) pkReference.getBoundNavigable() ).getIdAttribute();
		assertTrue( pkOrmSingularAttribute.isId() );
//		assertThat( pkReference.getExpressionType().getTypeName(), is( Integer.class.getName() ) );
	}

	@Test
	public void testNonAggregatedCompositeIdReference() {
		SqmSelectStatement sqm = interpretSelect( "select e.id from NonAggregatedCompositeIdEntityWithNonIdAttributeNamedId e" );
		SqmSingularAttributeBinding idReference = (SqmSingularAttributeBinding) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		SingularAttribute attribute = (SingularAttribute) idReference.getBoundNavigable();
		assertFalse( attribute.isId() );
//		assertThat( idReference.getExpressionType().getTypeName(), is(String.class.getName() ) );
	}
}
