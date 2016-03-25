/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributeBinding;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class CollectionSizeSqmExpression implements SqmExpression {
	AttributeBinding pluralAttributeBinding;
	private final BasicType resultType;

	public CollectionSizeSqmExpression(AttributeBinding pluralAttributeBinding, BasicType resultType) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.resultType = resultType;
	}

	public AttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	@Override
	public BasicType getExpressionType() {
		return resultType;
	}

	@Override
	public Type getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCollectionSizeFunction( this );
	}
}
