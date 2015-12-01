/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class FromElementReferenceExpression implements AttributePathPart, Expression {
	private final FromElement fromElement;
	private final Type type;

	public FromElementReferenceExpression(FromElement fromElement, Type type) {
		this.fromElement = fromElement;
		this.type = type;
	}

	public FromElement getFromElement() {
		return fromElement;
	}

	@Override
	public Type getExpressionType() {
		return type;
	}

	@Override
	public Type getInferableType() {
		return getExpressionType();
	}

	@Override
	public Bindable getBindableModelDescriptor() {
		return fromElement.getBindableModelDescriptor();
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		return fromElement;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitFromElementReferenceExpression( this );
	}
}
