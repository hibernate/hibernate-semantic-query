/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

/**
 * Models a reference to the collection's index (list-index / map-key)
 *
 * @deprecated {@link org.hibernate.persister.collection.spi.CollectionIndex}
 */
@Deprecated
public interface SqmPluralAttributeIndex extends SqmNavigable, SqmExpressableType {
	enum IndexClassification {
		BASIC,
		EMBEDDABLE,
		ANY,
		ONE_TO_MANY,
		MANY_TO_MANY
	}

	IndexClassification getClassification();
}
