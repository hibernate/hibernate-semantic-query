/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmExpressableTypeEmbedded;
import org.hibernate.sqm.domain.type.SqmDomainTypeEmbeddable;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class IndexedElementBindingEmbedded
		extends AbstractIndexedElementBinding
		implements SqmRestrictedCollectionElementBindingEmbedded {
	public IndexedElementBindingEmbedded(
			SqmPluralAttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression) {
		super( pluralAttributeBinding, indexSelectionExpression );
	}

	@Override
	public SqmExpressableTypeEmbedded getExpressionType() {
		return (SqmExpressableTypeEmbedded) getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmExpressableTypeEmbedded getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmDomainTypeEmbeddable getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}

	@Override
	public SqmExpressableTypeEmbedded getBoundNavigable() {
		return (SqmExpressableTypeEmbedded) super.getBoundNavigable();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return getPluralAttributeBinding().getExportedFromElement();
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		throw new NotYetImplementedException( "Cannot inject SqmFrom element into a CompositeBinding" );
	}
}
