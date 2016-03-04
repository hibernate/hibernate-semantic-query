/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;

/**
 * Models a reference to the value of a Collection or Map as defined by the JPA
 * {@code VALUE()} expression.  Similar to legacy Hibernate {@code ELEMENTS()}
 * expression except that {@code VALUE()} can be further dereferenced (it acts
 * as as AttributeBindingSource too).
 *
 * @author Steve Ebersole
 */
public class CollectionValuePathExpression implements Expression, AttributeBindingSource {
	private final QualifiedAttributeJoinFromElement pluralAttributeBinding;
	private final Type elementType;

	public CollectionValuePathExpression(QualifiedAttributeJoinFromElement pluralAttributeBinding, Type elementType) {
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

	@Override
	public FromElement getFromElement() {
		return pluralAttributeBinding;
	}

	@Override
	public ManagedType getAttributeContributingType() {
		return (ManagedType) elementType;
	}

	@Override
	public ManagedType getSubclassIndicator() {
		return getAttributeContributingType();
	}

	@Override
	public Bindable getBoundModelType() {
		// for now just cast.  
		return (Bindable) getAttributeContributingType();
	}

	@Override
	public String asLoggableText() {
		return "VALUE(" + pluralAttributeBinding.asLoggableText() + ")";
	}

	@Override
	public FromElementBinding getBoundFromElementBinding() {
		return null;
	}
}
