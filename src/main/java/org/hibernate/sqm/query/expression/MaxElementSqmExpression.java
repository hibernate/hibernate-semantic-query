/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class MaxElementSqmExpression implements SqmExpression {
	private final String collectionAlias;
	private final Type elementType;

	public MaxElementSqmExpression(FromElement collectionReference, Type elementType) {
		this.collectionAlias = collectionReference.getIdentificationVariable();
		this.elementType = elementType;
	}

	public String getCollectionAlias() {
		return collectionAlias;
	}

	public Type getElementType() {
		return elementType;
	}

	@Override
	public Type getExpressionType() {
		return getElementType();
	}

	@Override
	public Type getInferableType() {
		return getElementType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxElementFunction( this );
	}
}
