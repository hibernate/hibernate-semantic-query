/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.util.Optional;

import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeElementReference;
import org.hibernate.sqm.domain.type.DomainType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementImpl implements PluralAttributeElementReference {
	private final PluralSqmAttributeImpl pluralAttribute;
	private final ElementClassification classification;
	private final Type elementType;

	public PluralAttributeElementImpl(PluralSqmAttributeImpl pluralAttribute, ElementClassification classification, Type elementType) {
		this.pluralAttribute = pluralAttribute;
		this.classification = classification;
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
		return classification;
	}

	@Override
	public DomainType getType() {
		return getElementType();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		if ( classification == ElementClassification.MANY_TO_MANY
				|| classification == ElementClassification.ONE_TO_MANY ) {
			return Optional.of( (EntityReference) elementType );
		}

		return Optional.empty();
	}
}
