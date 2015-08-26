/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class FromElementReferenceExpression implements AttributePathPart, Expression {
	private final FromElement fromElement;

	public FromElementReferenceExpression(FromElement fromElement) {
		this.fromElement = fromElement;
	}

	public FromElement getFromElement() {
		return fromElement;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return fromElement.getTypeDescriptor();
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
