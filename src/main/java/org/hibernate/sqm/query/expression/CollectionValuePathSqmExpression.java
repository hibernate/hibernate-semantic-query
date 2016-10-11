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
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Models a reference to the value of a Collection or Map as defined by the JPA
 * {@code VALUE()} expression.  Similar to legacy Hibernate {@code ELEMENTS()}
 * expression except that {@code VALUE()} can be further dereferenced (it acts
 * as as AttributeBindingSource too).
 *
 * @author Steve Ebersole
 */
public class CollectionValuePathSqmExpression implements SqmExpression, Binding {
	private final SqmAttributeJoin pluralAttributeBinding;
	private final Type elementType;

	public CollectionValuePathSqmExpression(SqmAttributeJoin pluralAttributeBinding, Type elementType) {
		this.pluralAttributeBinding = pluralAttributeBinding;
		this.elementType = elementType;
	}

	public SqmAttributeJoin getPluralAttributeBinding() {
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
	public SqmFrom getFromElement() {
		return pluralAttributeBinding;
	}

	@Override
	public ManagedType getSubclassIndicator() {
		return (ManagedType) elementType;
	}

	@Override
	public Bindable getBindable() {
		// for now just cast.  
		return (Bindable) elementType;
	}

	@Override
	public String asLoggableText() {
		return "VALUE(" + pluralAttributeBinding.asLoggableText() + ")";
	}

}
