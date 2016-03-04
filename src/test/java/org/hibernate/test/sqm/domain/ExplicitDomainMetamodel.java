/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.TemporalType;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.IdentifiableType;

/**
 * @author Steve Ebersole
 */
public class ExplicitDomainMetamodel implements DomainMetamodel {
	private Map<String, String> importMap = new HashMap<String, String>();

	private Map<String, PolymorphicEntityTypeImpl> polymorphicEntityTypeMap = new HashMap<String, PolymorphicEntityTypeImpl>();

	private Map<String,EntityTypeImpl> entityTypeMap = new HashMap<String, EntityTypeImpl>();

	private Map<Class, BasicType> basicTypeMap = new HashMap<Class, BasicType>();

	public ExplicitDomainMetamodel() {
		// prime the basicTypeMap with the BasicTypeImpls from StandardBasicTypeDescriptors
		for ( Field field : StandardBasicTypeDescriptors.class.getDeclaredFields() ) {
			if ( BasicType.class.isAssignableFrom( field.getType() ) ) {
				try {
					final BasicType descriptor = (BasicType) field.get( StandardBasicTypeDescriptors.INSTANCE );
					basicTypeMap.put( descriptor.getJavaType(), descriptor );
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

//	public MappedSuperclassTypeImpl makeMappedSuperclassType(Class javaType) {
//		return makeMappedSuperclassType( javaType, null );
//	}
//
//	public MappedSuperclassTypeImpl makeMappedSuperclassType(Class javaType, IdentifiableType superType) {
//		return new MappedSuperclassTypeImpl( javaType, superType );
//	}

	public EntityTypeImpl makeEntityType(Class javaType) {
		return makeEntityType( javaType, null );
	}

	public EntityTypeImpl makeEntityType(Class javaType, IdentifiableType superType) {
		final EntityTypeImpl entityType = new EntityTypeImpl( javaType, superType );
		entityTypeMap.put( javaType.getName(), entityType );
		importMap.put( javaType.getSimpleName(), javaType.getName() );

		return entityType;
	}

	public EntityTypeImpl makeEntityType(String entityName) {
		return makeEntityType( entityName, null );
	}

	public EntityTypeImpl makeEntityType(String entityName, IdentifiableType superType) {
		if ( entityName.contains( "." ) ) {
			final String importedName = entityName.substring( entityName.lastIndexOf( '.' ) + 1 );
			importMap.put( importedName, entityName );
		}
		final EntityTypeImpl entityType = new EntityTypeImpl( entityName, superType );
		entityTypeMap.put( entityName, entityType );

		return entityType;
	}

	public PolymorphicEntityTypeImpl makePolymorphicEntity(Class entityClass) {
		final PolymorphicEntityTypeImpl entityTypeDescriptor = new PolymorphicEntityTypeImpl( entityClass );
		importMap.put( entityClass.getSimpleName(), entityTypeDescriptor.getTypeName() );
		polymorphicEntityTypeMap.put( entityClass.getName(), entityTypeDescriptor );
		return entityTypeDescriptor;
	}

	public PolymorphicEntityTypeImpl makePolymorphicEntity(String name) {
		if ( name.contains( "." ) ) {
			final String importedName = name.substring( name.lastIndexOf( '.' ) + 1 );
			importMap.put( importedName, name );
		}
		final PolymorphicEntityTypeImpl type = new PolymorphicEntityTypeImpl( name );
		polymorphicEntityTypeMap.put( name, type );
		return type;

	}

	public EmbeddableTypeImpl makeEmbeddableType(String embeddableName) {
		return new EmbeddableTypeImpl( embeddableName );
	}

	public EmbeddableTypeImpl makeEmbeddableType(Class embeddableClass) {
		return new EmbeddableTypeImpl( embeddableClass );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Class<T> javaType) {
		BasicType basicType = basicTypeMap.get( javaType );
		if ( basicType == null ) {
			basicType = new BasicTypeImpl( javaType );
			basicTypeMap.put( javaType, basicType );
		}
		return basicType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Class<T> javaType, TemporalType temporalType) {
		return getBasicType( javaType );
	}

	@Override
	public EntityType resolveEntityType(Class javaType) {
		final EntityTypeImpl entityType = entityTypeMap.get( javaType.getName() );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec" );
		}
		return entityType;
	}

	@Override
	public EntityType resolveEntityType(String name) {
		if ( importMap.containsKey( name ) ) {
			name = importMap.get( name );
		}
		EntityTypeImpl entityType = entityTypeMap.get( name );
		if ( entityType == null ) {
			entityType = polymorphicEntityTypeMap.get( name );
		}
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec : no entity named " + name );
		}
		return entityType;
	}
}
