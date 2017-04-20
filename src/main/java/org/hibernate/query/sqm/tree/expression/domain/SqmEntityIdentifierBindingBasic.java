/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.domain;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmEntityIdentifier;
import org.hibernate.query.sqm.domain.SqmExpressableType;
import org.hibernate.query.sqm.tree.SqmPropertyPath;

/**
 * @author Steve Ebersole
 */
public class SqmEntityIdentifierBindingBasic extends AbstractSqmNavigableBinding implements SqmEntityIdentifierBinding {
	private final SqmEntityTypedBinding source;
	private final SqmEntityIdentifier entityIdentifier;

	public SqmEntityIdentifierBindingBasic(SqmEntityTypedBinding source, SqmEntityIdentifier entityIdentifier) {
		this.source = source;
		this.entityIdentifier = entityIdentifier;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return entityIdentifier;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return entityIdentifier;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitEntityIdentifierBinding( this );
	}

	@Override
	public String asLoggableText() {
		return entityIdentifier.asLoggableText();
	}

	@Override
	public SqmNavigableSourceBinding getSourceBinding() {
		return source;
	}

	@Override
	public SqmEntityIdentifier getBoundNavigable() {
		return entityIdentifier;
	}

	@Override
	public SqmPropertyPath getPropertyPath() {
		return source.getPropertyPath().append( entityIdentifier.getNavigableName() );
	}
}
