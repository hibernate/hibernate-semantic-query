/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmPluralAttributeIndexEmbedded;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class CollectionIndexBindingEmbedded extends AbstractCollectionIndexBinding implements SqmEmbeddableTypedBinding {
	public CollectionIndexBindingEmbedded(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmPluralAttributeIndexEmbedded  getBoundNavigable() {
		return (SqmPluralAttributeIndexEmbedded ) super.getBoundNavigable();
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
