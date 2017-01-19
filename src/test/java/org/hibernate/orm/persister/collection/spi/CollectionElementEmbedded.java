/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.spi;

import org.hibernate.orm.persister.embeddable.spi.EmbeddableReference;

/**
 * @author Steve Ebersole
 */
public interface CollectionElementEmbedded extends CollectionElement, EmbeddableReference {
	@Override
	default PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	org.hibernate.orm.type.spi.EmbeddedType getOrmType();

	@Override
	default ElementClassification getClassification() {
		return ElementClassification.EMBEDDABLE;
	}
}
