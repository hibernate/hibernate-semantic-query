/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;

import org.hibernate.test.sqm.domain.PluralSqmAttributeImpl;
import org.hibernate.test.sqm.type.spi.AnyType;
import org.hibernate.test.sqm.type.spi.Attribute;
import org.hibernate.test.sqm.type.spi.BasicType;
import org.hibernate.test.sqm.type.spi.EmbeddableType;
import org.hibernate.test.sqm.type.spi.EntityType;
import org.hibernate.test.sqm.type.spi.ManagedType;
import org.hibernate.test.sqm.type.spi.SingularAttribute;
import org.hibernate.test.sqm.type.spi.Type;
import static org.hibernate.test.sqm.type.internal.JavaTypeHelper.resolveAttributeMember;
import org.jboss.logging.Logger;

/**
 * Base support for all ManagedType implementations.managed types, which is the JPA term for commonality between entity, embeddable and
 * "mapped superclass" types.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractManagedType extends AbstractTypeImpl implements ManagedType {

	// todo : make this TypeConfigurationAware upstream

	private static final Logger log = Logger.getLogger( AbstractManagedType.class );

	private Class javaType;
	private Map<String,Attribute> attributesByName;

	public AbstractManagedType(String typeName) {
		super( typeName );
		this.javaType = null;
	}

	public AbstractManagedType(Class javaType) {
		super( javaType.getName() );
		this.javaType = javaType;
	}

	public void setJavaType(Class javaType) {
		log.debugf(
				"setting javaType to [" + javaType.getName() + "], was [" + this.javaType + "]"
		);
		this.javaType = javaType;
	}


	public AbstractSingularAttribute makeSingularAttribute(String name, Type type) {

		// todo : figure out how to resolve (1) Disposition and (2) isOptional and (3) isManyToOne
		//		^^ these are all needed for JPA

		final AbstractSingularAttribute attribute;
		if ( type instanceof BasicType ) {
			attribute = new SingularAttributeBasicImpl(
					this,
					name,
					(BasicType) type,
					SingularAttribute.Disposition.NORMAL,
					resolveAttributeMember( javaType, name, type ),
					// optional?  JPA requires it here
					false
			);
		}
		else if ( type instanceof EmbeddableType ) {
			attribute = new SingularAttributeEmbeddedImpl(
					this,
					name,
					(EmbeddableType) type,
					SingularAttribute.Disposition.NORMAL,
					resolveAttributeMember( javaType, name, type )
			);
		}
		else if ( type instanceof EntityType ) {
			attribute = new SingularAttributeEntityImpl(
					this,
					name,
					(EntityType) type,
					SingularAttribute.Disposition.NORMAL,
					resolveAttributeMember( javaType, name, type ),
					// isManyToOne
					true
			);
		}
		else if ( type instanceof AnyType ) {
			throw new NotYetImplementedException(  );
		}
		else {
			throw new RuntimeException( "Unknown Type : " + type );
		}

		addAttribute( attribute );
		return attribute;
	}

	public PluralSqmAttributeImpl makeMapAttribute(
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

	public PluralSqmAttributeImpl makeMapAttribute(
			String name,
			ElementClassification elementClassification,
			Type keyType,
			Type elementType) {
		throw new NotYetImplementedException(  );
//		final PluralSqmAttributeImpl attr = new PluralSqmAttributeImpl(
//				this,
//				name,
//				PluralSqmAttribute.CollectionClassification.MAP,
//				elementClassification,
//				IndexClassification.BASIC,
//				null,
//				keyType,
//				elementType
//		);
//		addAttribute( attr );
//		return attr;
	}

	public PluralSqmAttributeImpl makeListAttribute(
			String name,
			BasicType indexType,
			Type elementType) {
		throw new NotYetImplementedException(  );
//		return makeListAttribute(
//				name,
//				inferElementClassification( elementType ),
//				indexType,
//				elementType
//		);
	}

	public PluralSqmAttributeImpl makeListAttribute(
			String name,
			ElementClassification elementClassification,
			BasicType indexType,
			Type elementType) {
		throw new NotYetImplementedException(  );
//		final PluralSqmAttributeImpl attr = new PluralSqmAttributeImpl(
//				this,
//				name,
//				PluralSqmAttribute.CollectionClassification.LIST,
//				elementClassification,
//				null,
//				null,
//				indexType,
//				elementType
//		);
//		addAttribute( attr );
//		return attr;
	}

	public PluralSqmAttributeImpl makeSetAttribute(
			String name,
			Type elementType) {
		return makeSetAttribute(
				name,
				inferElementClassification( elementType ),
				elementType
		);
	}

	public PluralSqmAttributeImpl makeSetAttribute(
			String name,
			ElementClassification elementClassification,
			Type elementType) {
		throw new NotYetImplementedException(  );
//		final PluralSqmAttributeImpl attr = new PluralSqmAttributeImpl(
//				this,
//				name,
//				PluralSqmAttribute.CollectionClassification.SET,
//				elementClassification,
//				null,
//				null,
//				null,
//				elementType
//		);
//		addAttribute( attr );
//		return attr;
	}

	protected void addAttribute(Attribute attribute) {
		if ( attributesByName == null ) {
			attributesByName = new HashMap<>();
		}
		attributesByName.put( attribute.getName(), attribute );
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
