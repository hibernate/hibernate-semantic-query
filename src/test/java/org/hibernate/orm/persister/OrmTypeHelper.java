/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister;

import java.util.Collections;

import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.common.spi.CompositeContainer;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.embeddable.internal.EmbeddableMapperImpl;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifiableTypeImplementor;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.type.descriptor.java.internal.EmbeddableJavaTypeDescriptorImpl;
import org.hibernate.orm.type.descriptor.java.spi.EmbeddableJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.orm.type.internal.EmbeddedTypeImpl;
import org.hibernate.orm.type.internal.EntityTypeImpl;
import org.hibernate.orm.type.spi.CollectionType;
import org.hibernate.orm.type.spi.EmbeddedType;
import org.hibernate.orm.type.spi.EntityType;
import org.hibernate.orm.type.spi.Type;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class OrmTypeHelper {
	private static final Logger log = Logger.getLogger( OrmTypeHelper.class );

	public static <T> Type convert(
			PersisterCreationContext creationContext,
			ManagedTypeImplementor source,
			String navigableName,
			Value valueBinding,
			TypeConfiguration typeConfiguration) {
		if ( valueBinding.getType() == null ) {
			return null;
		}

		if ( valueBinding.getType() instanceof org.hibernate.type.BasicType ) {
			return convertBasic( (BasicType) valueBinding.getType(), typeConfiguration );
		}

		if ( valueBinding.getType() instanceof org.hibernate.type.CompositeType ) {
			return convertEmbedded( creationContext, source, navigableName, (Component) valueBinding, typeConfiguration );
		}

		if ( valueBinding.getType() instanceof org.hibernate.type.CollectionType ) {
			return convertCollection( creationContext, source, navigableName, (Collection) valueBinding, typeConfiguration );
		}

		if ( valueBinding.getType() instanceof org.hibernate.type.ManyToOneType ) {
			return convertEntity( creationContext, source, navigableName, (ToOne) valueBinding, typeConfiguration );
		}

		throw new NotYetImplementedException( "Converting " + valueBinding.getType().getClass().getName() + " -> org.hibernate.orm.type.spi.Type" );
	}

	public static EntityType convertEntity(
			PersisterCreationContext creationContext,
			ManagedTypeImplementor source,
			String navigableName,
			ToOne entityValue,
			TypeConfiguration typeConfiguration) {
		final String entityName = entityValue.getReferencedEntityName();
		final EntityPersister persister = typeConfiguration.findEntityPersister( entityName );
		if ( persister == null ) {
			throw new IllegalStateException( "Could not resolve EntityPersister : " + entityName );
		}
		return new EntityTypeImpl( null, persister.getJavaTypeDescriptor(), creationContext.getTypeConfiguration() );
	}

	public static EntityType convertEntity(
			PersisterCreationContext creationContext,
			org.hibernate.type.EntityType upstreamType,
			TypeConfiguration typeConfiguration) {
		final String associatedEntityName = upstreamType.getAssociatedEntityName( creationContext.getSessionFactory() );
		EntityPersister persister = typeConfiguration.findEntityPersister( associatedEntityName );
		if ( persister == null ) {
			persister = creationContext.getPersisterFactory().createEntityPersister(
					creationContext.getMetadata().getEntityBinding( associatedEntityName ),
					null,
					null,
					creationContext
			);
		}
		return new EntityTypeImpl( null, persister.getJavaTypeDescriptor(), creationContext.getTypeConfiguration() );
	}

	@SuppressWarnings("unchecked")
	public static org.hibernate.orm.type.spi.BasicType convertBasic(Property mappingProperty, TypeConfiguration typeConfiguration) {
		return typeConfiguration.getBasicTypeRegistry().getBasicType( mappingProperty.getType().getReturnedClass() );
	}

	public static org.hibernate.orm.type.spi.BasicType convertBasic(
			BasicType type,
			TypeConfiguration typeConfiguration) {
		return typeConfiguration.getBasicTypeRegistry().getBasicType( type.getReturnedClass() );
	}

	private static EmbeddedType convertEmbedded(
			PersisterCreationContext creationContext,
			CompositeContainer source,
			String navigableName,
			Component embeddedValue,
			TypeConfiguration typeConfiguration) {
		final String roleName = source.getRolePrefix() + navigableName;
		EmbeddableMapper mapper = typeConfiguration.findEmbeddableMapper( roleName );
		if ( mapper == null ) {
			mapper = creationContext.getPersisterFactory().createEmbeddablePersister( embeddedValue, source, navigableName, creationContext );
			creationContext.registerEmbeddablePersister( mapper );
		}
		return mapper.getOrmType();
	}

	private static CollectionType convertCollection(
			PersisterCreationContext creationContext,
			ManagedTypeImplementor source,
			String navigableName,
			Collection collectionValue,
			TypeConfiguration typeConfiguration) {
		final String roleName = source.getRolePrefix() + navigableName;

		CollectionPersister<?, ?, ?> collectionPersister = typeConfiguration.findCollectionPersister( roleName );

		if ( collectionPersister == null ) {
			collectionPersister = creationContext.getPersisterFactory().createCollectionPersister(
					collectionValue,
					source,
					navigableName,
					null,
					creationContext
			);
		}

		return collectionPersister.getOrmType();
	}

	public static EmbeddedType convertComposite(
			PersisterCreationContext creationContext,
			String navigableName,
			Component navigableType,
			CompositeContainer source,
			TypeConfiguration typeConfiguration) {
		final String roleName = source.getRolePrefix() + navigableName;
		EmbeddableMapper mapper = typeConfiguration.findEmbeddableMapper( roleName );
		if ( mapper == null ) {
			mapper = creationContext.getPersisterFactory().createEmbeddablePersister(
					navigableType,
					source,
					navigableName,
					creationContext
			);
			creationContext.registerEmbeddablePersister( mapper );
		}
		return mapper.getOrmType();
	}
}
