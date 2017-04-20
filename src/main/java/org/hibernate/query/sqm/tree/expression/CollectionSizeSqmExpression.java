/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeBinding;

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
