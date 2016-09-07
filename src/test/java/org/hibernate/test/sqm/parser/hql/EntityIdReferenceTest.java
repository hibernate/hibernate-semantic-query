/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.SqmStatementSelect;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.SingleAttributeIdentifierDescriptor;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;
import static org.junit.Assert.assertThat;

/**
 * The Hibernate ORM persisters keep id descriptions specially.  Specifically they are
 * not part of the grouping of "normal attributes".  One ramification of this special
 * treatment of 'id' references in sqm.  These tests assert that we handle those
 * ramifications correctly.
 *
 * @author Steve Ebersole
 */
public class EntityIdReferenceTest {
	@Test
	public void testReferenceSimpleIdAttributeNamedId() {
		// NOTE: the EntityTypeImpl automatically adds the IdentifierDescriptor we need here
		// 		so we just use that one.
		final ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl personType = metamodel.makeEntityType( "com.acme.Person" );

		personType.makeSingularAttribute(
				"name",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);


		final ConsumerContextImpl consumerContext = new ConsumerContextImpl( metamodel );

		SqmStatementSelect sqm = (SqmStatementSelect) interpret( "select p.id from Person p", consumerContext );
		AttributeReferenceSqmExpression idReference = (AttributeReferenceSqmExpression) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( idReference.getExpressionType().getTypeName(), is(Integer.class.getName() ) );

		sqm = (SqmStatementSelect) interpret( "select p.pk from Person p", consumerContext );
		AttributeReferenceSqmExpression pkReference = (AttributeReferenceSqmExpression) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( pkReference.getExpressionType().getTypeName(), is(Integer.class.getName() ) );

		assertThat( idReference.getBoundAttribute(), sameInstance( pkReference.getBoundAttribute() ) );
	}

	@Test
	public void testEntityWithNonIdAttributeNamedId() {
		// NOTE: the EntityTypeImpl automatically adds the IdentifierDescriptor we need here
		// 		so we just use that one.
		//
		// So the difference here from the above test is that 'id' refers to the "normal attribute"
		// 		named id; to refer to the identifier we have to use its explicit attribute name

		final ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl personType = metamodel.makeEntityType( "com.acme.Person" );

		personType.makeSingularAttribute(
				"name",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		personType.makeSingularAttribute(
				"id",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);


		final ConsumerContextImpl consumerContext = new ConsumerContextImpl( metamodel );

		SqmStatementSelect sqm = (SqmStatementSelect) interpret( "select p.id from Person p", consumerContext );
		AttributeReferenceSqmExpression idReference = (AttributeReferenceSqmExpression) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( idReference.getExpressionType().getTypeName(), is( String.class.getName() ) );

		sqm = (SqmStatementSelect) interpret( "select p.pk from Person p", consumerContext );
		AttributeReferenceSqmExpression pkReference = (AttributeReferenceSqmExpression) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( pkReference.getExpressionType().getTypeName(), is( Integer.class.getName() ) );
	}

	@Test
	public void testNonAggregatedCompositeIdReference() {
		// this test mimics how Hibernate ORM persister manages the entity model.  The
		// identifier is kept separate from the "normal" attributes.  Make sure SQM
		// interpretation handles this properly by using the identifier instead (as opposed
		// to complaining that the attribute can not be found/
		final ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

		EntityTypeImpl personType = metamodel.makeEntityType( "com.acme.Person" );

		personType.setIdentifierDescriptor(
				new SingleAttributeIdentifierDescriptor(
						personType,
						"<id>",
						StandardBasicTypeDescriptors.INSTANCE.INTEGER
				)
		);

		personType.makeSingularAttribute(
				"name",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		personType.makeSingularAttribute(
				"id",
				SingularAttribute.Classification.BASIC,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);


		final ConsumerContextImpl consumerContext = new ConsumerContextImpl( metamodel );

		SqmStatementSelect sqm = (SqmStatementSelect) interpret( "select p.id from Person p", consumerContext );
		AttributeReferenceSqmExpression idReference = (AttributeReferenceSqmExpression) sqm.getQuerySpec().getSelectClause().getSelections().get( 0 ).getExpression();
		assertThat( idReference.getExpressionType().getTypeName(), is(String.class.getName() ) );
	}
}
