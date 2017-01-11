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
public class MinIndexBindingEntity extends AbstractSingularIndexBinding implements MinIndexBinding,
		SqmEntityTypedBinding {
	private static final Logger log = Logger.getLogger( MinIndexBindingEntity.class );

	private SqmFrom exportedFromElement;

	public MinIndexBindingEntity(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmPluralAttributeIndexEntity getBoundNavigable() {
		return (SqmPluralAttributeIndexEntity) super.getBoundNavigable();
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getBoundNavigable();
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
