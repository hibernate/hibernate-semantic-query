/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.tree.SqmPropertyPath;
import org.hibernate.query.sqm.tree.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class SqmEntityIdentifierBindingEmbedded
		extends AbstractSqmNavigableBinding
		implements SqmEntityIdentifierBinding, SqmEmbeddableTypedBinding {

	private final SqmEntityTypedBinding sourceBinding;
	private final SqmEntityIdentifierEmbedded sqmNavigable;

	public SqmEntityIdentifierBindingEmbedded(SqmEntityTypedBinding sourceBinding, SqmEntityIdentifierEmbedded sqmNavigable) {
		this.sourceBinding = sourceBinding;
		this.sqmNavigable = sqmNavigable;
	}

	@Override
	public SqmFrom getExportedFromElement() {
		return sourceBinding.getExportedFromElement();
	}

	@Override
	public void injectExportedFromElement(SqmFrom sqmFrom) {
		sourceBinding.injectExportedFromElement( sqmFrom );
	}

	@Override
	public SqmNavigableSourceBinding getSourceBinding() {
		return sourceBinding;
	}

	@Override
	public SqmEntityIdentifierEmbedded getBoundNavigable() {
		return sqmNavigable;
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return getSourceBinding().getPropertyPath().append( sqmNavigable.getNavigableName() );
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return sqmNavigable;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return sqmNavigable;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityIdentifierBinding( this );
	}

	@Override
	public String asLoggableText() {
		return sqmNavigable.asLoggableText();
	}
}
