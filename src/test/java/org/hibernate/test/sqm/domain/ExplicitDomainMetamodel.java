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

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ExplicitDomainMetamodel implements DomainMetamodel {
	private static final Logger log = Logger.getLogger( ExplicitDomainMetamodel.class );

	private Map<String, String> importMap = new HashMap<>();
	private Map<String,EntityTypeImpl> entityTypeMap = new HashMap<>();
	private Map<String, PolymorphicEntityTypeImpl> polymorphicEntityTypeMap = new HashMap<>();
	private Map<Class, BasicType> basicTypeMap = new HashMap<>();

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

	@Override
	public EntityReference resolveEntityReference(String entityName) {
		if ( importMap.containsKey( entityName ) ) {
			entityName = importMap.get( entityName );
		}
		EntityTypeImpl entityType = entityTypeMap.get( entityName );
		if ( entityType == null ) {
			entityType = polymorphicEntityTypeMap.get( entityName );
		}
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec : no entity named " + entityName );
		}
		return entityType;
	}

	@Override
	public EntityReference resolveEntityReference(Class javaType) {
		final EntityTypeImpl entityType = entityTypeMap.get( javaType.getName() );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec" );
		}
		return entityType;
	}

	@Override
	public AttributeReference resolveAttributeReference(DomainReference source, String attributeName) {
		final Type type = extractType( source, Type.class );
		if ( !ManagedType.class.isInstance( type ) ) {
			throw new IllegalArgumentException( "Passed DomainReference [" + source + "] not known to expose attributes" );
		}

		return ( (ManagedType) type ).findAttribute( attributeName );
	}

	@Override
	public BasicType resolveBasicType(Class javaType) {
		BasicType basicType = basicTypeMap.get( javaType );
		if ( basicType == null ) {
			basicType = new BasicTypeImpl( javaType );
			basicTypeMap.put( javaType, basicType );
		}
		return basicType;
	}

	@Override
	public BasicType resolveArithmeticType(
			DomainReference firstType,
			DomainReference secondType,
			BinaryArithmeticSqmExpression.Operation operation) {
		return ExpressionTypeHelper.resolveArithmeticType(
				extractType( firstType, org.hibernate.test.sqm.domain.BasicType.class ),
				extractType( secondType, org.hibernate.test.sqm.domain.BasicType.class ),
				operation == BinaryArithmeticSqmExpression.Operation.DIVIDE,
				this
		);
	}

	@SuppressWarnings("unchecked")
	private <T extends Type> T extractType(DomainReference reference, Class<T> expectedType) {
		if ( reference == null ) {
			log.warn( "DomainReference from which to extract Type was null" );
			return null;
		}

		Type type = null;
		if ( reference instanceof Type ) {
			type = (Type) reference;
		}
		else if ( reference instanceof SingularAttribute ) {
			type = ( (SingularAttribute) reference ).getType();
		}
		else if ( reference instanceof PluralAttribute ) {
			type = ( (PluralAttribute) reference ).getElementType();
		}
		else if ( reference instanceof PluralAttributeElementImpl ) {
			type = ( (PluralAttributeElementImpl) reference ).getElementType();
		}
		else if ( reference instanceof PluralAttributeIndexImpl ) {
			type = ( (PluralAttributeIndexImpl) reference ).getIndexType();
		}
		else {
			throw new IllegalArgumentException( "Unsure how to extract Type from given DomainReference [" + reference + "]" );
		}

		if ( type == null ) {
			log.warnf( "Resolving DomainReference [%s] to Type resulted in null", reference );
			return null;
		}

		if ( !expectedType.isInstance( type ) ) {
			throw new IllegalArgumentException(
					"Type [" + type + "] extracted from DomainReference [" + reference + "] did not match expected Type [" + expectedType + "]"
			);
		}

		return (T) type;
	}

	@Override
	public BasicType resolveSumFunctionType(DomainReference argumentType) {
		return ExpressionTypeHelper.resolveSingleNumericType(
				extractType( argumentType, org.hibernate.test.sqm.domain.BasicType.class ),
				this
		);
	}

	@Override
	public BasicType resolveCastTargetType(String name) {
		throw new NotYetImplementedException(  );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization

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

}
