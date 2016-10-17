/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Models references to plural attributes (persistent collections)
 *
 * @author Steve Ebersole
 */
public interface PluralAttributeReference extends AttributeReference {
	/**
	 * Classifications of the plurality
	 */
	enum CollectionClassification {
		SET,
		LIST,
		MAP,
		BAG
	}

	CollectionClassification getCollectionClassification();

	/**
	 * Models a reference to the collection's element(s)
	 */
	interface ElementReference extends DomainReference {
		enum ElementClassification {
			BASIC,
			EMBEDDABLE,
			ANY,
			ONE_TO_MANY,
			MANY_TO_MANY
		}

		ElementClassification getClassification();

		DomainReference getType();
	}

	ElementReference getElementReference();

	/**
	 * Models a reference to the collection's index (list-index / map-key)
	 */
	interface IndexReference extends DomainReference {
		enum IndexClassification {
			BASIC,
			EMBEDDABLE,
			ANY,
			ONE_TO_MANY,
			MANY_TO_MANY
		}

		IndexClassification getClassification();

		DomainReference getType();
	}

	IndexReference getIndexReference();
}
