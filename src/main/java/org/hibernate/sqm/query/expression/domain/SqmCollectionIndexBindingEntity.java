/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmPluralAttributeIndexEntity;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmCollectionIndexBindingEntity
		extends AbstractSqmCollectionIndexBinding
		implements SqmNavigableSourceBinding, SqmEntityTypedBinding {
	private static final Logger log = Logger.getLogger( SqmCollectionIndexBindingEntity.class );

	private SqmFrom exportedFromElement;

	public SqmCollectionIndexBindingEntity(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmPluralAttributeIndexEntity getBoundNavigable() {
		return (SqmPluralAttributeIndexEntity) getPluralAttributeBinding().getBoundNavigable().getIndexReference();
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into CollectionElementBindingEntity [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}
}
