/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.domain.SqmPluralAttributeIndexEmbedded;
import org.hibernate.query.sqm.tree.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmMaxIndexBindingEmbedded extends AbstractSpecificSqmCollectionIndexBinding implements SqmMaxIndexBinding,
		SqmEmbeddableTypedBinding {
	private static final Logger log = Logger.getLogger( SqmMaxIndexBindingEmbedded.class );

	private SqmFrom exportedFromElement;

	public SqmMaxIndexBindingEmbedded(SqmPluralAttributeBinding pluralAttributeBinding) {
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
				"Injecting SqmFrom [%s] into MaxIndexBindingEmbedded [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}
}
