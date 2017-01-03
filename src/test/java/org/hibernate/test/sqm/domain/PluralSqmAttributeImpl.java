/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.PluralAttributeElementReference;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.domain.PluralAttributeIndexReference;

/**
 * @author Steve Ebersole
 */
public class PluralSqmAttributeImpl implements PluralSqmAttribute {
	private final CollectionClassification collectionClassification;
	private final ManagedType declaringType;
	private final String name;
	private final BasicType collectionIdType;
	private final PluralAttributeIndexImpl indexDescriptor;
	private final PluralAttributeElementImpl elementDescriptor;

	public PluralSqmAttributeImpl(
			ManagedType declaringType,
			String name,
			CollectionClassification collectionClassification,
			ElementClassification elementClassification,
			PluralAttributeIndexReference.IndexClassification indexClassification,
			BasicType collectionIdType,
			Type collectionIndexType,
			Type collectionElementType) {
		this.collectionClassification = collectionClassification;
		this.declaringType = declaringType;
		this.name = name;
		this.collectionIdType = collectionIdType;
		this.indexDescriptor = indexClassification == null ? null : new PluralAttributeIndexImpl( this, indexClassification, collectionIndexType );
		this.elementDescriptor = new PluralAttributeElementImpl( this, elementClassification, collectionElementType );
	}

	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	@Override
	public ManagedType getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getAttributeName() {
		return name;
	}

	@Override
	public Type getBoundType() {
		return elementDescriptor.getElementType();
	}

	@Override
	public ManagedType asManagedType() {
		// todo : for now, just let the ClassCastException happen
		return (ManagedType) elementDescriptor;
	}

	@Override
	public PluralAttributeElementReference getElementReference() {
		return elementDescriptor;
	}

	@Override
	public Type getElementType() {
		return elementDescriptor.getElementType();
	}

	@Override
	public PluralAttributeIndexReference getIndexReference() {
		return indexDescriptor;
	}

	@Override
	public Type getIndexType() {
		return indexDescriptor.getIndexType();
	}

	@Override
	public BasicType getCollectionIdType() {
		return collectionIdType;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttribute(" + declaringType.getTypeName() + "." + name + ")";
	}

}
