/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.tree.SqmPropertyPath;
import org.hibernate.query.sqm.tree.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class SqmEntityBinding extends AbstractSqmNavigableBinding
		implements SqmNavigableBinding, SqmNavigableSourceBinding, SqmEntityTypedBinding {
	private static final Logger log = Logger.getLogger( SqmEntityBinding.class );

	private final SqmNavigableSourceBinding sourceBinding;
	private final SqmExpressableTypeEntity entityReference;
	private SqmPropertyPath propertyPath;

	private SqmFrom exportedFromElement;

	public SqmEntityBinding(SqmExpressableTypeEntity entityReference) {
		this.entityReference = entityReference;
		this.sourceBinding = null;
		this.propertyPath = new SqmPropertyPath( null, entityReference.getEntityName() );
	}

	public SqmEntityBinding(SqmNavigableSourceBinding sourceBinding, SqmExpressableTypeEntity entityReference) {
		this.sourceBinding = sourceBinding;
		this.entityReference = entityReference;

		this.propertyPath = new SqmPropertyPath( sourceBinding.getPropertyPath(), entityReference.getEntityName() );
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return exportedFromElement;
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		log.debugf(
				"Injecting SqmFrom [%s] into EntityBindingImpl [%s], was [%s]",
				sqmFrom,
				this,
				this.exportedFromElement
		);
		exportedFromElement = sqmFrom;
		propertyPath = new SqmPropertyPath( null, entityReference.getEntityName() + "(" + sqmFrom.getIdentificationVariable() + ")" );
	}

	@Override
	public SqmNavigableSourceBinding getSourceBinding() {
		return sourceBinding;
	}

	@Override
	public SqmExpressableTypeEntity getBoundNavigable() {
		return entityReference;
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public SqmExpressableTypeEntity getExpressionType() {
		return entityReference;
	}

	@Override
	public SqmExpressableTypeEntity getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return (T) exportedFromElement.getBinding();
//		return exportedFromElement.accept( walker );
	}

	@Override
	public String asLoggableText() {
		return entityReference.asLoggableText();
	}
}
