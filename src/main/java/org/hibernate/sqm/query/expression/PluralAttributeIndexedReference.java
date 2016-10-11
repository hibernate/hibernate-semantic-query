/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributeBinding;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexedReference implements Binding {
	private final AttributeBinding pluralAttributeBinding;
	private final SqmExpression indexSelectionExpression;

	private final Type type;

	public PluralAttributeIndexedReference(
			AttributeBinding pluralAttributeBinding,
			SqmExpression indexSelectionExpression,
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

	public SqmExpression getIndexSelectionExpression() {
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
	public Bindable getBindable() {
		return getPluralAttributeBinding().getBindable();
	}

	@Override
	public String asLoggableText() {
		return pluralAttributeBinding.asLoggableText();
	}

	private boolean isBindable() {
		return type instanceof ManagedType;
	}

	@Override
	public ManagedType getSubclassIndicator() {
		return isBindable()
				? (ManagedType) type
				: null;
	}

	@Override
	public SqmFrom getFromElement() {
		return pluralAttributeBinding.getFromElement();
	}
}
