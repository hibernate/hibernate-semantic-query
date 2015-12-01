/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeImpl implements PluralAttribute {
	private final CollectionClassification collectionClassification;
	private final ElementClassification elementClassification;
	private final ManagedType declaringType;
	private final String name;
	private final BasicType collectionIdType;
	private final Type collectionIndexType;
	private final Type collectionElementType;

	public PluralAttributeImpl(
			CollectionClassification collectionClassification,
			ElementClassification elementClassification,
			ManagedType declaringType,
			String name,
			BasicType collectionIdType,
			Type collectionIndexType,
			Type collectionElementType) {
		this.collectionClassification = collectionClassification;
		this.elementClassification = elementClassification;
		this.declaringType = declaringType;
		this.name = name;
		this.collectionIdType = collectionIdType;
		this.collectionIndexType = collectionIndexType;
		this.collectionElementType = collectionElementType;
	}

	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	@Override
	public ElementClassification getElementClassification() {
		return elementClassification;
	}

	@Override
	public BasicType getCollectionIdType() {
		return collectionIdType;
	}

	@Override
	public Type getCollectionIndexType() {
		return collectionIndexType;
	}

	@Override
	public Type getCollectionElementType() {
		return collectionElementType;
	}

	@Override
	public ManagedType getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getBoundType() {
		return collectionElementType;
	}

	@Override
	public ManagedType asManagedType() {
		// todo : for now, just let the ClassCastException happen
		return (ManagedType) collectionElementType;
	}
}
