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
class PluralAttributeElementImpl implements PluralAttributeReference.ElementReference {
	private final PluralAttributeImpl pluralAttribute;
	private final ElementClassification elementClassification;
	private final Type elementType;

	public PluralAttributeElementImpl(PluralAttributeImpl pluralAttribute, ElementClassification elementClassification, Type elementType) {
		this.pluralAttribute = pluralAttribute;
		this.elementClassification = elementClassification;
		this.elementType = elementType;
	}

	public Type getElementType() {
		return elementType;
	}

	@Override
	public String asLoggableText() {
		return "values_or_elements(" + pluralAttribute.asLoggableText() + ")";
	}

	@Override
	public ElementClassification getClassification() {
		return elementClassification;
	}

	@Override
	public DomainReference getType() {
		return getElementType();
	}
}
