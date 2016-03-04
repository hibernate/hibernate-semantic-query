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
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.FromElementBinding;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class MapKeyPathExpression implements Expression, AttributeBindingSource {
	private final FromElement collectionReference;
	private final Type indexType;

	public MapKeyPathExpression(FromElement collectionReference, Type indexType) {
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
	public FromElement getFromElement() {
		return collectionReference;
	}

	@Override
	public ManagedType getAttributeContributingType() {
		return (ManagedType) getMapKeyType();
	}

	@Override
	public ManagedType getSubclassIndicator() {
		if ( !ManagedType.class.isInstance( getMapKeyType() ) ) {
			return null;
		}
		return (ManagedType) getMapKeyType();
	}

	@Override
	public Bindable getBoundModelType() {
		if ( !Bindable.class.isInstance( getMapKeyType() ) ) {
			return null;
		}
		return (Bindable) getMapKeyType();
	}

	@Override
	public String asLoggableText() {
		return "MapKeyPathExpression(" + getFromElement().asLoggableText() + ")";
	}

	@Override
	public FromElementBinding getBoundFromElementBinding() {
		return null;
	}
}
