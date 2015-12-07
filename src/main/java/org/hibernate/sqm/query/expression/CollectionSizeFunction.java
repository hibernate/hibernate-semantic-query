/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Represents the {@code SIZE()} function.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class CollectionSizeFunction implements Expression {
	private final String fromElementAlias;
	private final Attribute attributeDescriptor;
	private final BasicType resultType;

	public CollectionSizeFunction(
			FromElement fromElement,
			Attribute attributeDescriptor,
			BasicType resultType) {
		this.fromElementAlias = fromElement.getAlias();
		this.attributeDescriptor = attributeDescriptor;
		this.resultType = resultType;
	}

	public String getFromElementAlias() {
		return fromElementAlias;
	}

	public Attribute getAttributeDescriptor() {
		return attributeDescriptor;
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
