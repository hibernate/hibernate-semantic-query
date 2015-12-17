/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;

/**
 * @author Steve Ebersole
 */
public class CollectionValueFunction implements Expression {
	private final QualifiedAttributeJoinFromElement pluralAttributeBinding;
	private final Type elementType;

	public CollectionValueFunction(QualifiedAttributeJoinFromElement pluralAttributeBinding, Type elementType) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.elementType = elementType;
	}

	public QualifiedAttributeJoinFromElement getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	public Type getElementType() {
		return elementType;
	}

	public Type getValueType() {
		return getElementType();
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
		return walker.visitCollectionValueFunction( this );
	}
}
