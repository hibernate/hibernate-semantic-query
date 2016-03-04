/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser.hql;

import org.hibernate.sqm.domain.DomainMetamodel;

import org.hibernate.test.query.parser.ConsumerContextImpl;
import org.hibernate.test.sqm.domain.EmbeddableTypeImpl;
import org.hibernate.test.sqm.domain.EntityTypeImpl;
import org.hibernate.test.sqm.domain.ExplicitDomainMetamodel;
import org.hibernate.test.sqm.domain.StandardBasicTypeDescriptors;
import org.junit.Test;

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
