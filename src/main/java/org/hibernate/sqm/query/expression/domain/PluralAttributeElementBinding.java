/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

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
	private final PluralAttributeBinding attributeBinding;
	private final PluralAttributeReference pluralAttributeReference;

	public PluralAttributeElementBinding(PluralAttributeBinding pluralAttributeBinding) {
		this.attributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = pluralAttributeBinding.getAttribute();
	}

	public PluralAttributeBinding getPluralAttributeBinding() {
		return attributeBinding;
	}

	public PluralAttributeReference getPluralAttributeReference() {
		return pluralAttributeReference;
	}

	@Override
	public SqmFrom getFromElement() {
		return attributeBinding.getFromElement();
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
		return "VALUE(" + attributeBinding.asLoggableText() + ")";
	}
}
