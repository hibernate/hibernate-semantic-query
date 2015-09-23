/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.CollectionTypeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class MaxElementFunction implements Expression {
	private final String collectionAlias;
	private final TypeDescriptor valueType;

	public MaxElementFunction(FromElement collectionReference) {
		this.collectionAlias = collectionReference.getAlias();

		CollectionTypeDescriptor collectionTypeDescriptor = (CollectionTypeDescriptor) collectionReference.getTypeDescriptor();
		this.valueType = collectionTypeDescriptor.getElementTypeDescriptor();
	}

	public String getCollectionAlias() {
		return collectionAlias;
	}

	public TypeDescriptor getValueType() {
		return valueType;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return getValueType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMaxElementFunction( this );
	}
}
