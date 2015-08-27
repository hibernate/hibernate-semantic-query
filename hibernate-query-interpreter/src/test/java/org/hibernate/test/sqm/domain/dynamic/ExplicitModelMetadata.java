/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain.dynamic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.CollectionTypeDescriptor;
import org.hibernate.sqm.domain.CompositeTypeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.ModelMetadata;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ExplicitModelMetadata implements ModelMetadata {
	private Map<String, String> importMap = new HashMap<String, String>();

	private Map<String, PolymorphicEntityTypeDescriptorImpl> polymorphicEntityTypeMap = new HashMap<String, PolymorphicEntityTypeDescriptorImpl>();
	private Map<String, EntityTypeDescriptorImpl> entityTypeMap = new HashMap<String, EntityTypeDescriptorImpl>();

	private Map<Class, BasicTypeDescriptor> basicTypeMap = new HashMap<Class, BasicTypeDescriptor>();

	public ExplicitModelMetadata() {
		for ( Field field : StandardBasicTypeDescriptors.class.getDeclaredFields() ) {
			if ( BasicTypeDescriptor.class.isAssignableFrom( field.getType() ) ) {
				try {
					final BasicTypeDescriptor descriptor = (BasicTypeDescriptor) field.get( StandardBasicTypeDescriptors.INSTANCE );
					basicTypeMap.put( descriptor.getCorrespondingJavaType(), descriptor );
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		final String importedName = importMap.containsKey( reference )
				? importMap.get( reference )
				: reference;

		if ( entityTypeMap.containsKey( importedName ) ) {
			return entityTypeMap.get( importedName );
		}

		return polymorphicEntityTypeMap.get( importedName );
	}

	public EntityTypeDescriptorImpl entity(String name) {
		EntityTypeDescriptorImpl descriptor = entityTypeMap.get( name );
		if ( descriptor == null ) {
			descriptor = new EntityTypeDescriptorImpl( name );
			entityTypeMap.put( name, descriptor );
		}
		return descriptor;
	}

	public EntityTypeDescriptorImpl entity(Class entityClass) {
		EntityTypeDescriptorImpl entityTypeDescriptor = entity( entityClass.getName() );
		importMap.put( entityClass.getSimpleName(), entityTypeDescriptor.getTypeName() );
		return entityTypeDescriptor;
	}

	public PolymorphicEntityTypeDescriptorImpl polymorphicEntity(String name) {
		if ( polymorphicEntityTypeMap.containsKey( name ) ) {
			return polymorphicEntityTypeMap.get( name );
		}

		PolymorphicEntityTypeDescriptorImpl descriptor = new PolymorphicEntityTypeDescriptorImpl( name );
		polymorphicEntityTypeMap.put( name, descriptor );
		return descriptor;
	}

	public PolymorphicEntityTypeDescriptorImpl polymorphicEntity(Class entityClass) {
		PolymorphicEntityTypeDescriptorImpl entityTypeDescriptor = polymorphicEntity( entityClass.getName() );
		importMap.put( entityClass.getSimpleName(), entityTypeDescriptor.getTypeName() );
		return entityTypeDescriptor;
	}

	public BasicTypeDescriptor basic(Class type) {
		BasicTypeDescriptor descriptor = basicTypeMap.get( type );
		if ( descriptor == null ) {
			descriptor = new BasicTypeDescriptorImpl( type );
			basicTypeMap.put( type, descriptor );
		}
		return descriptor;
	}

	public CompositeTypeDescriptorImpl composite(String type) {
		return new CompositeTypeDescriptorImpl( type );
	}

	public CompositeTypeDescriptorImpl composite(Class type) {
		return new CompositeTypeDescriptorImpl( type.getName() );
	}

	public static class BasicTypeDescriptorImpl implements BasicTypeDescriptor {
		private final Class javaType;

		public BasicTypeDescriptorImpl(Class javaType) {
			this.javaType = javaType;
		}

		@Override
		public Class getCorrespondingJavaType() {
			return javaType;
		}

		@Override
		public String getTypeName() {
			return javaType.getName();
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			// basic types cannot have persistent attributes
			return null;
		}
	}

	public static class CompositeTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements CompositeTypeDescriptor {
		public CompositeTypeDescriptorImpl(String typeName) {
			super( typeName );
		}
	}

	public static class CollectionTypeDescriptorImpl
			implements CollectionTypeDescriptor {
		private final String collectionTypeName;
		private final TypeDescriptor indexType;
		private final TypeDescriptor elementType;

		public CollectionTypeDescriptorImpl(TypeDescriptor elementType) {
			this( Collection.class, null, elementType );
		}

		public CollectionTypeDescriptorImpl(TypeDescriptor indexType, TypeDescriptor elementType) {
			this( Map.class, indexType, elementType );
		}

		public CollectionTypeDescriptorImpl(Class collectionType, TypeDescriptor indexType, TypeDescriptor elementType) {
			collectionTypeName = collectionType.getName();
			this.indexType = indexType;
			this.elementType = elementType;
		}

		@Override
		public String getTypeName() {
			return collectionTypeName;
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			return null;
		}

		@Override
		public TypeDescriptor getIndexTypeDescriptor() {
			return indexType;
		}

		@Override
		public TypeDescriptor getElementTypeDescriptor() {
			return elementType;
		}
	}

	public static abstract class AbstractTypeDescriptorImpl implements TypeDescriptor {
		private final String typeName;
		private final Map<String, AttributeDescriptorImpl> attributeMap = new HashMap<String, AttributeDescriptorImpl>();

		public AbstractTypeDescriptorImpl(String typeName) {
			this.typeName = typeName;
		}

		@Override
		public String getTypeName() {
			return typeName;
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			return attributeMap.get( attributeName );
		}

		public void addAttribute(AttributeDescriptorImpl attribute) {
			attributeMap.put( attribute.getName(), attribute );
		}

		public void addAttribute(String name, TypeDescriptor type) {
			addAttribute( new AttributeDescriptorImpl( name, type ) );
		}

		public Map<String, AttributeDescriptorImpl> getAttributeMap() {
			return attributeMap;
		}
	}

	public static class EntityTypeDescriptorImpl extends AbstractTypeDescriptorImpl implements EntityTypeDescriptor {
		public EntityTypeDescriptorImpl(String typeName) {
			super( typeName );
		}
	}

	public static class PolymorphicEntityTypeDescriptorImpl implements PolymorphicEntityTypeDescriptor {
		private final String typeName;
		private List<EntityTypeDescriptor> implementors = new ArrayList<EntityTypeDescriptor>();

		private Map<String, AttributeDescriptorImpl> attributeMap;

		public PolymorphicEntityTypeDescriptorImpl(String typeName) {
			this.typeName = typeName;
		}

		@Override
		public Collection<EntityTypeDescriptor> getImplementors() {
			return implementors;
		}

		@Override
		public String getTypeName() {
			return typeName;
		}

		public void addImplementor(EntityTypeDescriptor implementor) {
			implementors.add( implementor );
		}
		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			if ( attributeMap == null ) {
				attributeMap = buildAttributeMap();
			}
			return attributeMap.get( attributeName );
		}

		private Map<String, AttributeDescriptorImpl> buildAttributeMap() {
			// The attribute must exist in all of them.  So we use the first implementor as a driver
			// and collect any attributes that exist across all implementors

			Map<String, AttributeDescriptorImpl> attributeMap = new HashMap<String, AttributeDescriptorImpl>();
			EntityTypeDescriptorImpl firstImplementor = (EntityTypeDescriptorImpl) implementors.get( 0 );
			attr_loop: for ( AttributeDescriptorImpl attributeDescriptor : firstImplementor.getAttributeMap().values() ) {
				for ( EntityTypeDescriptor implementor : implementors ) {
					if ( implementor.getAttributeDescriptor(  attributeDescriptor.getName() ) == null ) {
						break attr_loop;
					}
				}

				// if we get here, every implementor defined that attribute...
				attributeMap.put( attributeDescriptor.getName(), attributeDescriptor );
			}

			return attributeMap;
		}
	}

	public static class AttributeDescriptorImpl implements AttributeDescriptor {
		private final String name;
		private final TypeDescriptor type;

		public AttributeDescriptorImpl(String name, TypeDescriptor type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public TypeDescriptor getType() {
			return type;
		}
	}
}
