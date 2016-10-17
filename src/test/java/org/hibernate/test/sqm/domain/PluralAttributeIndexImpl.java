/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;

/**
 * @author Steve Ebersole
 */
class PluralAttributeIndexImpl implements PluralAttributeReference.IndexReference {
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
	public DomainReference getType() {
		return getIndexType();
	}
}
