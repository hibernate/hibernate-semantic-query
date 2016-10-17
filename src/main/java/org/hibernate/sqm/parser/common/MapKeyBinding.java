/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class MapKeyBinding implements DomainReferenceBinding {
	private final AttributeBinding pluralAttributeBinding;
	private final PluralAttributeReference pluralAttributeReference;

	public MapKeyBinding(AttributeBinding pluralAttributeBinding) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = (PluralAttributeReference) pluralAttributeBinding.getAttribute();

		assert pluralAttributeReference.getCollectionClassification() == PluralAttributeReference.CollectionClassification.MAP;
	}

	public AttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	@Override
	public SqmFrom getFromElement() {
		return pluralAttributeBinding.getFromElement();
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return pluralAttributeReference.getIndexReference().getType();
	}

	@Override
	public DomainReference getExpressionType() {
		return getBoundDomainReference();
	}

	@Override
	public DomainReference getInferableType() {
		return getBoundDomainReference();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapKeyBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "KEY(" + pluralAttributeBinding.asLoggableText() + ")";
	}
}
