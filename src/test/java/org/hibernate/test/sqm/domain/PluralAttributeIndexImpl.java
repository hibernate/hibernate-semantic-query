/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.util.Optional;

import org.hibernate.sqm.domain.Navigable;
import org.hibernate.sqm.domain.EntityDescriptor;
import org.hibernate.sqm.domain.PluralAttributeIndexDescriptor;

/**
 * @author Steve Ebersole
 */
class PluralAttributeIndexImpl implements PluralAttributeIndexDescriptor {
	private final PluralAttributeImpl pluralAttribute;
	private final IndexClassification classification;
	private final Type indexType;

	public PluralAttributeIndexImpl(PluralAttributeImpl pluralAttribute, IndexClassification classification, Type indexType) {
		this.pluralAttribute = pluralAttribute;
		this.classification = classification;
		this.indexType = indexType;
	}

	public Type getIndexType() {
		return indexType;
	}

	@Override
	public String asLoggableText() {
		return "index_or_key(" + pluralAttribute.asLoggableText() + ")";
	}

	@Override
	public IndexClassification getClassification() {
		return classification;
	}

	@Override
	public Navigable getType() {
		return getIndexType();
	}

	@Override
	public Optional<EntityDescriptor> toEntityReference() {
		if ( classification == IndexClassification.MANY_TO_MANY
				|| classification == IndexClassification.ONE_TO_MANY ) {
			return Optional.of( (EntityDescriptor) indexType );
		}

		return Optional.empty();
	}
}
