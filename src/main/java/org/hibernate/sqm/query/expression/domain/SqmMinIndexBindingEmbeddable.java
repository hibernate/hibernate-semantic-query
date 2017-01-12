/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmPluralAttributeIndexEmbedded;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmMinIndexBindingEmbeddable extends AbstractSpecificSqmCollectionIndexBinding implements SqmMinIndexBinding,
		SqmEmbeddableTypedBinding {
	private static final Logger log = Logger.getLogger( SqmMinIndexBindingEmbeddable.class );

	private SqmFrom exportedFromElement;

	public SqmMinIndexBindingEmbeddable(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmPluralAttributeIndexEmbedded getBoundNavigable() {
		return (SqmPluralAttributeIndexEmbedded) super.getBoundNavigable();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into MinIndexBindingEmbeddable [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}
}
