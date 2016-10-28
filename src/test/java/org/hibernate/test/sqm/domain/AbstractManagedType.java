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

import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.domain.PluralAttributeIndexReference.IndexClassification;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;

/**
 * Base support for all ManagedType implementations.managed types, which is the JPA term for commonality between entity, embeddable and
 * "mapped superclass" types.
 *
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

	public SingularAttributeImpl makeSingularAttribute(String name, Type type) {
		final SingularAttributeClassification classification;
		if ( type instanceof BasicType ) {
			classification = SingularAttributeClassification.BASIC;
		}
		else if ( type instanceof EmbeddableType ) {
			classification = SingularAttributeClassification.EMBEDDED;
		}
		else if ( type instanceof AnyType ) {
			classification = SingularAttributeClassification.ANY;
		}
		else {
			classification = SingularAttributeClassification.MANY_TO_ONE;
		}
		return makeSingularAttribute( name, classification, type );
	}

	public SingularAttributeImpl makeSingularAttribute(
			String name,
			SingularAttributeClassification classification,
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

	private ElementClassification inferElementClassification(Type elementType) {
		if ( elementType instanceof BasicType ) {
			return ElementClassification.BASIC;
		}
		else if ( elementType instanceof EmbeddableType ) {
			return ElementClassification.EMBEDDABLE;
		}
		else if ( elementType instanceof AnyType ) {
			return ElementClassification.ANY;
		}
		else {
			return ElementClassification.ONE_TO_MANY;
		}
	}

	public PluralAttributeImpl makeMapAttribute(
			String name,
			ElementClassification elementClassification,
			Type keyType,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				this,
				name,
				PluralAttribute.CollectionClassification.MAP,
				elementClassification,
				IndexClassification.BASIC,
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
			ElementClassification elementClassification,
			BasicType indexType,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				this,
				name,
				PluralAttribute.CollectionClassification.LIST,
				elementClassification,
				null,
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
			ElementClassification elementClassification,
			Type elementType) {
		final PluralAttributeImpl attr = new PluralAttributeImpl(
				this,
				name,
				PluralAttribute.CollectionClassification.SET,
				elementClassification,
				null,
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
		attributesByName.put( attribute.getAttributeName(), attribute );
	}

	@Override
	public Attribute findAttribute(String name) {
		if ( attributesByName != null ) {
			if ( attributesByName.containsKey( name ) ) {
				return attributesByName.get( name );
			}
		}

		return null;
	}

	@Override
	public Attribute findDeclaredAttribute(String name) {
		return findAttribute( name );
	}

	Map<String, Attribute> getAttributesByName() {
		return attributesByName == null ? Collections.<String, Attribute>emptyMap() : attributesByName;
	}
}
