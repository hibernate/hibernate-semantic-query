/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain.dynamic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.hibernate.sqm.domain.AttributeDescriptor;
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
public class DynamicModelMetadata implements ModelMetadata {
	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		if ( reference.startsWith( "Polymorphic" ) ) {
			final String baseName = reference.substring( 11 );
			final EntityTypeDescriptorImpl first = new EntityTypeDescriptorImpl( baseName + "1" );
			final EntityTypeDescriptorImpl second = new EntityTypeDescriptorImpl( baseName + "2" );
			return new PolymorphicEntityTypeDescriptorImpl(
					reference,
					first,
					second
			);
		}
		else {
			return new EntityTypeDescriptorImpl( reference );
		}
	}

	public static abstract class AbstractTypeDescriptorImpl implements TypeDescriptor {
		private final String typeName;

		public AbstractTypeDescriptorImpl(String typeName) {
			this.typeName = typeName;
		}

		@Override
		public String getTypeName() {
			return typeName;
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			if ( attributeName.startsWith( "basicCollection" ) ) {
				return buildBasicCollectionAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "basicMap" ) ) {
				return buildBasicMapAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "basic" ) ) {
				return buildBasicAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "composite" ) ) {
				return buildCompositeAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "entity" ) ) {
				return buildEntityAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "collection" ) ) {
				return buildCollectionAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "map" ) ) {
				return buildMapAttribute( attributeName );
			}

			return null;
		}

		protected AttributeDescriptor buildBasicAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					StandardBasicTypeDescriptors.INSTANCE.LONG
			);
		}

		protected AttributeDescriptor buildCompositeAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new CompositeTypeDescriptorImpl("attribute: " + attributeName)
			);
		}

		protected AttributeDescriptor buildEntityAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new EntityTypeDescriptorImpl( "attribute: " + attributeName)
			);
		}

		protected AttributeDescriptor buildCollectionAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new CollectionTypeDescriptorImpl(
							new EntityTypeDescriptorImpl( "collection-value:" + attributeName )
					)
			);
		}

		protected AttributeDescriptor buildBasicCollectionAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new CollectionTypeDescriptorImpl( StandardBasicTypeDescriptors.INSTANCE.LONG )
			);
		}

		protected AttributeDescriptor buildMapAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new CollectionTypeDescriptorImpl(
							StandardBasicTypeDescriptors.INSTANCE.LONG,
							new EntityTypeDescriptorImpl( "map-element:" + attributeName )
					)
			);
		}

		protected AttributeDescriptor buildBasicMapAttribute(String attributeName) {
			return new AttributeDescriptorImpl(
					attributeName,
					new CollectionTypeDescriptorImpl(
							StandardBasicTypeDescriptors.INSTANCE.LONG,
							StandardBasicTypeDescriptors.INSTANCE.LONG
					)
			);
		}
	}

	public static class EntityTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements EntityTypeDescriptor {
		public EntityTypeDescriptorImpl(String entityName) {
			super( entityName.contains( "." ) ? entityName :  "com.acme." + entityName );
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

	private class PolymorphicEntityTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements PolymorphicEntityTypeDescriptor {
		private final Collection<EntityTypeDescriptor> implementors;

		public PolymorphicEntityTypeDescriptorImpl(
				String entityName,
				EntityTypeDescriptor... implementors) {
			super( entityName.contains( "." ) ? entityName :  "com.acme." + entityName );
			this.implementors = Arrays.asList( implementors );
		}

		@Override
		public Collection<EntityTypeDescriptor> getImplementors() {
			return implementors;
		}

		@Override
		public AttributeDescriptor getAttributeDescriptor(String attributeName) {
			AttributeDescriptor attributeDescriptor = null;
			for ( EntityTypeDescriptor implementor : implementors ) {
				attributeDescriptor = implementor.getAttributeDescriptor( attributeName );
				if ( attributeDescriptor == null ) {
					return null;
				}
			}
			return attributeDescriptor;
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
			extends AbstractTypeDescriptorImpl
			implements CollectionTypeDescriptor {
		private final TypeDescriptor indexType;
		private final TypeDescriptor elementType;

		public CollectionTypeDescriptorImpl(TypeDescriptor elementType) {
			this( Collection.class, null, elementType );
		}

		public CollectionTypeDescriptorImpl(TypeDescriptor indexType, TypeDescriptor elementType) {
			this( Map.class, indexType, elementType );
		}

		public CollectionTypeDescriptorImpl(Class collectionType, TypeDescriptor indexType, TypeDescriptor elementType) {
			super( collectionType.getName() );
			this.indexType = indexType;
			this.elementType = elementType;
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
}
