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
 * Represents the ENTRY() function for obtaining the map entries from a {@code Map}-typed association.
 *
 * @author Gunnar Morling
 */
public class MapEntryFunction implements Expression {
	private final String collectionAlias;
	private final TypeDescriptor indexType;
	private final TypeDescriptor valueType;

	public MapEntryFunction(FromElement collectionReference) {
		this.collectionAlias = collectionReference.getAlias();

		CollectionTypeDescriptor collectionTypeDescriptor = (CollectionTypeDescriptor) collectionReference.getTypeDescriptor();
		this.indexType = collectionTypeDescriptor.getIndexTypeDescriptor();
		this.valueType = collectionTypeDescriptor.getElementTypeDescriptor();
	}

	public String getCollectionAlias() {
		return collectionAlias;
	}

	public TypeDescriptor getMapKeyType() {
		return indexType;
	}

	public TypeDescriptor getMapValueType() {
		return valueType;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return getMapKeyType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMapEntryFunction( this );
	}
}
