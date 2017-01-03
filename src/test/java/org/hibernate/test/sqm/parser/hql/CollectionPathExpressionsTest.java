/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.expression.domain.PluralAttributeElementBinding;
import org.hibernate.sqm.query.select.SqmSelection;

import org.hibernate.test.sqm.ConsumerContextImpl;
import org.hibernate.test.sqm.type.internal.EmbeddableTypeImpl;
import org.hibernate.test.sqm.type.internal.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.sqm.SemanticQueryInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class CollectionPathExpressionsTest {
	private final ConsumerContextImpl consumerContext = new ConsumerContextImpl( buildMetamodel() );

	@Test
	public void testMapKeyPath() {
		interpret( "select p from Phrase p where key( p.translations ).language = 'en'", consumerContext );
	}

	@Test
	public void testCollectionReferenceAsSelection() {
		// essentially, assert that a raw reference to a plural attribute is
		//		implicitly handled as a reference to the elements, in the
		// 		select clause anyway
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( "select t from Phrase p join p.translations t", consumerContext );
		assertThat( statement.getQuerySpec().getSelectClause().getSelections().size(), is(1) );

		final SqmSelection selection = statement.getQuerySpec().getSelectClause().getSelections().get( 0 );
		assertThat( selection.getExpression(), instanceOf( PluralAttributeElementBinding.class ) );
	}

	@Test
	public void testMapIndexedAccess() {
		interpret( "select t.mapLegs['LA'].id from Trip t", consumerContext );
	}

	private DomainMetamodel buildMetamodel() {
		ExplicitDomainMetamodel metamodel = new ExplicitDomainMetamodel();

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


		EmbeddableTypeImpl localeType = metamodel.makeEmbeddableType( "com.acme.Locale" );
		localeType.makeSingularAttribute(
				"language",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);
		localeType.makeSingularAttribute(
				"country",
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		EntityTypeImpl phraseType = metamodel.makeEntityType( "com.acme.Phrase" );
		phraseType.makeMapAttribute(
				"translations",
				localeType,
				StandardBasicTypeDescriptors.INSTANCE.STRING
		);

		return metamodel;
	}
}
