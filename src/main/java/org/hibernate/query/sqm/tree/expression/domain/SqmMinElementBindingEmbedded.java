/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEmbedded;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEmbeddable;
import org.hibernate.query.sqm.tree.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmMinElementBindingEmbedded extends AbstractSpecificSqmElementBinding implements SqmMinElementBinding,
		SqmEmbeddableTypedBinding {
	private static final Logger log = Logger.getLogger( SqmMinElementBindingEmbedded.class );

	private SqmFrom exportedFromElement;

	public SqmMinElementBindingEmbedded(SqmPluralAttributeBinding pluralAttributeBinding) {
		super( pluralAttributeBinding );
	}

	@Override
	public SqmExpressableTypeEmbedded getExpressionType() {
		return (SqmExpressableTypeEmbedded) getPluralAttributeBinding().getBoundNavigable().getElementReference();
	}

	@Override
	public SqmExpressableType getInferableType() {
		return null;
	}

	@Override
	public SqmExpressableTypeEmbedded getBoundNavigable() {
		return (SqmExpressableTypeEmbedded) super.getBoundNavigable();
	}

	@Override
	public SqmDomainTypeEmbeddable getExportedDomainType() {
		return (SqmDomainTypeEmbeddable) super.getExportedDomainType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return null;
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into MinElementBindingEmbedded [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
	}
}
