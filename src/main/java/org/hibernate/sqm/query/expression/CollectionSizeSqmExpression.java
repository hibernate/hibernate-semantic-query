/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.query.expression.domain.SqmPluralAttributeBinding;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class CollectionSizeSqmExpression implements SqmExpression {
	private final SqmPluralAttributeBinding pluralAttributeBinding;
	private final SqmExpressableTypeBasic sizeType;

	public CollectionSizeSqmExpression(SqmPluralAttributeBinding pluralAttributeBinding, SqmExpressableTypeBasic sizeType) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.sizeType = sizeType;
	}

	public SqmPluralAttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	@Override
	public SqmExpressableTypeBasic getExpressionType() {
		return sizeType;
	}

	@Override
	public SqmExpressableTypeBasic getInferableType() {
		return getExpressionType();
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return getExpressionType().getExportedDomainType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitPluralAttributeSizeFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "SIZE(" + pluralAttributeBinding.asLoggableText() + ")";
	}
}
