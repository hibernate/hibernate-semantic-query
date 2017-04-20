/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEntity;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmMinElementBindingEntity extends AbstractSpecificSqmElementBinding implements SqmMinElementBinding,
		SqmEntityTypedBinding {
	private static final Logger log = Logger.getLogger( SqmMinElementBindingEntity.class );

	private SqmFrom exportedFromElement;

	public SqmMinElementBindingEntity(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return getBoundNavigable();
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmExpressableTypeEntity getBoundNavigable() {
		return (SqmExpressableTypeEntity) getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmDomainTypeEntity getExportedDomainType() {
		return (SqmDomainTypeEntity) super.getExportedDomainType();
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into MinElementBindingEntity [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return null;
	}
}
