/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmExpressableType;
import org.hibernate.sqm.domain.SqmPluralAttribute;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.query.PropertyPath;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionElementBinding extends AbstractNavigableBinding implements CollectionElementBinding {
	private final SqmPluralAttributeBinding attributeBinding;
	private final SqmPluralAttribute pluralAttributeReference;
	private final PropertyPath propertyPath;

	public AbstractCollectionElementBinding(SqmPluralAttributeBinding pluralAttributeBinding) {
		this.attributeBinding = pluralAttributeBinding;
		this.pluralAttributeReference = pluralAttributeBinding.getBoundNavigable();

		this.propertyPath = pluralAttributeBinding.getPropertyPath().append( "{elements}" );
	}

	public SqmPluralAttributeBinding getPluralAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public SqmPluralAttributeBinding getSourceBinding() {
		return getPluralAttributeBinding();
	}

	@Override
	public SqmNavigable getBoundNavigable() {
		return getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getBoundNavigable().getExportedDomainType();
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String asLoggableText() {
		return getPropertyPath().getFullPath();
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return getPluralAttributeBinding().getBoundNavigable();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}
}
