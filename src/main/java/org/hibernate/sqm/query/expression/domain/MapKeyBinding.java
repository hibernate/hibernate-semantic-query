/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralSqmAttributeReference;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class MapKeyBinding implements DomainReferenceBinding {
	private final PluralAttributeBinding attributeBinding;
	private final PluralSqmAttributeReference pluralAttributeReference;
	private final PropertyPath propertyPath;

	public MapKeyBinding(PluralAttributeBinding pluralAttributeBinding) {
		this.attributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = pluralAttributeBinding.getAttribute();

		assert pluralAttributeReference.getCollectionClassification() == PluralSqmAttributeReference.CollectionClassification.MAP;

		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{keys}" );
	}

	public PluralAttributeBinding getPluralAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public SqmFrom getFromElement() {
		return attributeBinding.getFromElement();
	}

	@Override
	public DomainReference getBoundDomainReference() {
		return pluralAttributeReference.getIndexReference().getType();
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
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
		return "KEY(" + attributeBinding.asLoggableText() + ")";
	}
}
