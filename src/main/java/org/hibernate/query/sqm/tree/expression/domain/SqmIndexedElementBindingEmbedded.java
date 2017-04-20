/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.NotYetImplementedException;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEmbedded;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEmbeddable;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class SqmIndexedElementBindingEmbedded
		extends AbstractSqmIndexedElementBinding
		implements SqmRestrictedCollectionElementBindingEmbedded {
	public SqmIndexedElementBindingEmbedded(
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
