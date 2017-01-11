/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.query.expression.domain;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.query.PropertyPath;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class EntityBindingImpl extends AbstractNavigableBinding
		implements SqmNavigableBinding, SqmNavigableSourceBinding, SqmEntityTypedBinding {
	private static final Logger log = Logger.getLogger( EntityBindingImpl.class );

	private final SqmNavigableSourceBinding sourceBinding;
	private final SqmExpressableTypeEntity entityReference;
	private PropertyPath propertyPath;

	private SqmFrom exportedFromElement;

	public EntityBindingImpl(SqmExpressableTypeEntity entityReference) {
		this.entityReference = entityReference;
		this.sourceBinding = null;
		this.propertyPath = new PropertyPath( null, entityReference.getEntityName() );
	}

	public EntityBindingImpl(SqmNavigableSourceBinding sourceBinding, SqmExpressableTypeEntity entityReference) {
		this.sourceBinding = sourceBinding;
		this.entityReference = entityReference;

		this.propertyPath = new PropertyPath( sourceBinding.getPropertyPath(), entityReference.getEntityName() );
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
		propertyPath = new PropertyPath( null, entityReference.getEntityName() + "(" + sqmFrom.getIdentificationVariable() + ")" );
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
	public PropertyPath getPropertyPath() {
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
