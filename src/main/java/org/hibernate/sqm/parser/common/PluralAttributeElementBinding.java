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
 * Models an explicit reference to the elements of the collection (generally via the VALUES() function).
 *
 * @author Steve Ebersole
 */
public class PluralAttributeElementBinding implements DomainReferenceBinding {
	private final AttributeBinding pluralAttributeBinding;
	private final PluralAttributeReference pluralAttributeReference;

	public PluralAttributeElementBinding(AttributeBinding pluralAttributeBinding) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = (PluralAttributeReference) pluralAttributeBinding.getAttribute();
	}

	public AttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	public PluralAttributeReference getPluralAttributeReference() {
		return pluralAttributeReference;
	}

	@Override
	public SqmFrom getFromElement() {
		return pluralAttributeBinding.getFromElement();
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return pluralAttributeReference.getElementReference();
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
		return walker.visitCollectionValueBinding( this );
	}

	@Override
	public String asLoggableText() {
		return "VALUE(" + pluralAttributeBinding.asLoggableText() + ")";
	}
}
