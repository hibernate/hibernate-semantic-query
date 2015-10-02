/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.AttributeDescriptor;
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
	private final AttributeDescriptor attributeDescriptor;

	public TypeFunction(FromElement reference) {
		this.fromElementAlias = reference.getAlias();
		this.attributeDescriptor = null;
		this.expression = null;
	}

	public TypeFunction(FromElement reference, AttributeDescriptor attributeDescriptor) {
		this.fromElementAlias = reference.getAlias();
		this.attributeDescriptor = attributeDescriptor;
		this.expression = null;
	}

	public TypeFunction(NamedParameterExpression namedParameterExpression) {
		this.fromElementAlias = null;
		this.attributeDescriptor = null;
		this.expression = namedParameterExpression;
	}

	public TypeFunction(PositionalParameterExpression positionalParameterExpression) {
		this.fromElementAlias = null;
		this.attributeDescriptor = null;
		this.expression = positionalParameterExpression;
	}

	public TypeFunction(MapKeyFunction mapKeyFunction) {
		this.fromElementAlias = null;
		this.attributeDescriptor = null;
		this.expression = mapKeyFunction;
	}

	public TypeFunction(CollectionValueFunction collectionValueFunction) {
		this.fromElementAlias = null;
		this.attributeDescriptor = null;
		this.expression = collectionValueFunction;
	}

	/**
	 * Returns the from element alias in case of {@code TYPE(alias)} or {@code TYPE(alias.property)}.
	 */
	public String getFromElementAlias() {
		return fromElementAlias;
	}

	/**
	 * Returns the attribute descriptor in case of {@code TYPE(alias.property)}.
	 */
	public AttributeDescriptor getAttributeDescriptor() {
		return attributeDescriptor;
	}

	/**
	 * Returns the sub-expression in case of {@code TYPE(:param)}, {@code TYPE(?1)}, {@code TYPE(KEY(map-alias))},
	 * {@code TYPE(VALUE(map-alias))}.
	 */
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
