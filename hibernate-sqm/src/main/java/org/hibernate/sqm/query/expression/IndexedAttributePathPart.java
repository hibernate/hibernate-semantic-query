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
public class IndexedAttributePathPart implements AttributePathPart, Expression {
	private final AttributePathPart source;
	private final Expression index;

	private final Type type;

	public IndexedAttributePathPart(AttributePathPart source, Expression index, Type type) {
		this.source = source;
		this.index = index;
		this.type = type;
	}

	public AttributePathPart getSource() {
		return source;
	}

	public Expression getIndex() {
		return index;
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
		return source.getBindableModelDescriptor();
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		// todo : almost positive this is not accurate in most cases
		return source.getUnderlyingFromElement();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException( "see todo comment" );
	}
}
