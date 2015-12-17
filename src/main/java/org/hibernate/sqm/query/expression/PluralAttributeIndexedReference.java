/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.FromElementBinding;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexedReference implements AttributeBinding, Expression {
	private final AttributeBinding pluralAttributeBinding;
	private final Expression indexSelectionExpression;

	private final Type type;

	public PluralAttributeIndexedReference(
			AttributeBinding pluralAttributeBinding,
			Expression indexSelectionExpression,
			Type type) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.indexSelectionExpression = indexSelectionExpression;
		this.type = type;
	}

	public AttributeBinding getPluralAttributeBinding() {
		return pluralAttributeBinding;
	}

	public PluralAttribute getPluralAttribute() {
		return (PluralAttribute) getPluralAttributeBinding().getBoundAttribute();
	}

	public Expression getIndexSelectionExpression() {
		return indexSelectionExpression;
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
	public <T> T accept(SemanticQueryWalker<T> walker) {
		throw new UnsupportedOperationException( "see todo comment" );
	}

	@Override
	public Attribute getBoundAttribute() {
		return getPluralAttribute();
	}

	@Override
	public AttributeBindingSource getAttributeBindingSource() {
		return getPluralAttributeBinding().getAttributeBindingSource();
	}

	@Override
	public Bindable getBoundModelType() {
		return getPluralAttributeBinding().getBoundModelType();
	}

	@Override
	public String asLoggableText() {
		return pluralAttributeBinding.asLoggableText();
	}

	@Override
	public FromElementBinding getBoundFromElementBinding() {
		return getAttributeBindingSource().getFromElement();
	}
}
