/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import java.util.Optional;

/**
 * Models references to plural attributes (persistent collections)
 *
 * @author Steve Ebersole
 */
public interface PluralSqmAttributeReference extends SqmAttributeReference, PotentialEntityReferenceExporter {
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

	PluralAttributeElementReference getElementReference();

	PluralAttributeIndexReference getIndexReference();

	@Override
	default Optional<EntityReference> toEntityReference() {
		return getElementReference().toEntityReference();
	}

	String getRole();
}
