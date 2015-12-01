/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * @author Steve Ebersole
 */
public interface PluralAttribute extends Attribute, Bindable {
	enum CollectionClassification {
		SET,
		LIST,
		MAP,
		BAG
	}

	/**
	 * both Hibernate and JPA define this in terms of the collection itself,
	 * but really it describe the collection's element classification
	 */
	enum ElementClassification {
		BASIC,
		EMBEDDABLE,
		ANY,
		ONE_TO_MANY,
		MANY_TO_MANY
	}

	CollectionClassification getCollectionClassification();
	ElementClassification getElementClassification();

	BasicType getCollectionIdType();
	Type getCollectionIndexType();
	Type getCollectionElementType();
}
