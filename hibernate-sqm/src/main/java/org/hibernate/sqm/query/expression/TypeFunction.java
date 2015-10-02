/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Represents the {@code TYPE()} function.
 *
 * @author Gunnar Morling
 */
public class TypeFunction implements Expression {

	private final String fromElementAlias;
	private final Expression expression;

	public TypeFunction(FromElement reference) {
		this.fromElementAlias = reference.getAlias();
		this.expression = null;
	}

	public TypeFunction(NamedParameterExpression namedParameterExpression) {
		this.fromElementAlias = null;
		this.expression = namedParameterExpression;
	}

	public TypeFunction(PositionalParameterExpression positionalParameterExpression) {
		this.fromElementAlias = null;
		this.expression = positionalParameterExpression;
	}

	public TypeFunction(MapKeyFunction mapKeyFunction) {
		this.fromElementAlias = null;
		this.expression = mapKeyFunction;
	}

	public TypeFunction(CollectionValueFunction collectionValueFunction) {
		this.fromElementAlias = null;
		this.expression = collectionValueFunction;
	}

	public String getFromElementAlias() {
		return fromElementAlias;
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return StandardBasicTypeDescriptors.INSTANCE.CLASS;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitTypeFunction( this );
	}
}
