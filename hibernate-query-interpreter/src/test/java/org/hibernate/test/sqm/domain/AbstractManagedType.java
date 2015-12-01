/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.sqm.domain.AnyType;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.EmbeddableType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractManagedType extends AbstractTypeImpl implements ManagedType {
	private Map<String,Attribute> attributesByName;

	public AbstractManagedType(String typeName) {
		super( typeName );
	}

	public AbstractManagedType(Class javaType) {
		super( javaType.getName() );
	}

	public SingularAttributeImpl makeSingularAttribute(
			String name,
			Type type) {
		final SingularAttribute.Classification classification;
		if ( type instanceof BasicType ) {
			classification = SingularAttribute.Classification.BASIC;
		}
		else if ( type instanceof EmbeddableType ) {
			classification = SingularAttribute.Classification.EMBEDDED;
		}
		else if ( type instanceof AnyType ) {
			classification = SingularAttribute.Classification.ANY;
		}
		else {
			classification = SingularAttribute.Classification.MANY_TO_ONE;
		}
		return makeSingularAttribute( name, classification, type );
	}

	public SingularAttributeImpl makeSingularAttribute(
			String name,
			SingularAttribute.Classification classification,
			Type type) {
		final SingularAttributeImpl attr = new SingularAttributeImpl(
				this,
				name,
				classification,
				type
		);
		addAttribute( attr );
		return attr;
	}

	public PluralAttributeImpl makeMapAttribute(
			String name,
			Type keyType,
			Type elementType) {
		return makeMapAttribute(
				name,
				inferElementClassification( elementType ),
				keyType,
				elementType
		);
	}

	private PluralAttribute.ElementClassification inferElementClassification(Type elementType) {
		if ( elementType instanceof BasicType ) {
			return PluralAttribute.ElementClassification.BASIC;
		}
		else if ( elementType instanceof EmbeddableType ) {
			return PluralAttribute.ElementClassification.EMBEDDABLE;
		}
		else if ( elementType instanceof AnyType ) {
			return PluralAttribute.ElementClassification.ANY;
		}
		else {
			return PluralAttribute.ElementClassification.ONE_TO_MANY;
		}
	}

	public PluralAttributeImpl makeMapAttribute(
			String name,
			PluralAttribute.ElementClassification elementClassification,
			Type keyType,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				PluralAttribute.CollectionClassification.MAP,
				elementClassification,
				this,
				name,
				null,
				keyType,
				elementType
		);
		addAttribute( attr );
		return attr;
	}

	public PluralAttributeImpl makeListAttribute(
			String name,
			BasicType indexType,
			Type elementType) {
		return makeListAttribute(
				name,
				inferElementClassification( elementType ),
				indexType,
				elementType
		);
	}

	public PluralAttributeImpl makeListAttribute(
			String name,
			PluralAttribute.ElementClassification elementClassification,
			BasicType indexType,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				PluralAttribute.CollectionClassification.LIST,
				elementClassification,
				this,
				name,
				null,
				indexType,
				elementType
		);
		addAttribute( attr );
		return attr;
	}

	public PluralAttributeImpl makeSetAttribute(
			String name,
			Type elementType) {
		return makeSetAttribute(
				name,
				inferElementClassification( elementType ),
				elementType
		);
	}

	public PluralAttributeImpl makeSetAttribute(
			String name,
			PluralAttribute.ElementClassification elementClassification,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				PluralAttribute.CollectionClassification.SET,
				elementClassification,
				this,
				name,
				null,
				null,
				elementType
		);
		addAttribute( attr );
		return attr;
	}

	protected void addAttribute(Attribute attribute) {
		if ( attributesByName == null ) {
			attributesByName = new HashMap<String, Attribute>();
		}
		attributesByName.put( attribute.getName(), attribute );
	}

	@Override
	public Attribute getAttribute(String name) {
		return attributesByName == null
				? null
				: attributesByName.get( name );
	}

	@Override
	public Attribute getDeclaredAttribute(String name) {
		return getAttribute( name );
	}

	Map<String, Attribute> getAttributesByName() {
		return attributesByName == null ? Collections.<String, Attribute>emptyMap() : attributesByName;
	}
}
