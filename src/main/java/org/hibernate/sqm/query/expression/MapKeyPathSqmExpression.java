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
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public class MapKeyPathSqmExpression implements SqmExpression, Binding {
	private final SqmFrom collectionReference;
	private final Type indexType;

	public MapKeyPathSqmExpression(SqmFrom collectionReference, Type indexType) {
		this.collectionReference = collectionReference;
		this.indexType = indexType;
	}

	public String getCollectionAlias() {
		return collectionReference.getIdentificationVariable();
	}

	public Type getMapKeyType() {
		return indexType;
	}

	@Override
	public Type getExpressionType() {
		return getMapKeyType();
	}

	@Override
	public Type getInferableType() {
		return getMapKeyType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapKeyFunction( this );
	}

	@Override
	public SqmFrom getFromElement() {
		return collectionReference;
	}

	@Override
	public ManagedType getSubclassIndicator() {
		if ( !ManagedType.class.isInstance( getMapKeyType() ) ) {
			return null;
		}
		return (ManagedType) getMapKeyType();
	}

	@Override
	public Bindable getBindable() {
		if ( !Bindable.class.isInstance( getMapKeyType() ) ) {
			return null;
		}
		return (Bindable) getMapKeyType();
	}

	@Override
	public String asLoggableText() {
		return "MapKeyPathExpression(" + getFromElement().asLoggableText() + ")";
	}
}
