/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.orm.persister.collection.internal.CollectionPersisterImpl;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.common.internal.PersisterHelper;
import org.hibernate.orm.persister.common.spi.CompositeContainer;
import org.hibernate.orm.persister.common.spi.ManagedTypeImplementor;
import org.hibernate.orm.persister.embeddable.internal.EmbeddableMapperImpl;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.entity.internal.EntityHierarchyImpl;
import org.hibernate.orm.persister.entity.internal.EntityPersisterImpl;
import org.hibernate.orm.persister.entity.spi.EntityHierarchy;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.entity.spi.IdentifiableTypeImplementor;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.persister.spi.PersisterFactory;
import org.hibernate.orm.type.descriptor.java.internal.EntityJavaTypeDescriptorImpl;
import org.hibernate.orm.type.descriptor.java.spi.EntityJavaTypeDescriptor;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.sqm.NotYetImplementedException;

/**
 * The standard ORM implementation of the {@link PersisterFactory} contract
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public final class PersisterFactoryImpl implements PersisterFactory, ServiceRegistryAwareService {
	private ServiceRegistryImplementor serviceRegistry;

	private Set<EntityHierarchyNode> roots = new HashSet<>();
	private Map<String,EntityHierarchyNode> nameToHierarchyNodeMap = new HashMap<>();
	private Map<EmbeddableMapper,Component> embeddableComponentMap = new HashMap<>();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// IMPL NOTE
	//
	// 	As a PersisterFactory implementation this class is responsible for
	//	generated both EntityPersister and CollectionPersister.
	//
	// todo : add creation of EmbeddablePersisters to PersisterFactory contract?
	//
	// The general flow for persister creation is as follows:
	//		1) #createPersister is called directly from the SessionFactory's
	//			Metamodel object during initialization.  It creates the
	//			EntityPersister instance and begins categorizing them in
	// 			relation to their hierarchy (nameToHierarchyNodeMap, roots) for
	// 			later processing
	//		2) After #createPersister has been called for all defined entities,
	//			#finishUp will be called.  This is the trigger to walk all the
	//			previously created persisters *in a specific order*: starting
	// 			from #roots we walk down each hierarchy meaning that as we
	//			perform EntityPersister#finishInitialization processing we know
	//			that the super is completely initialized.  Part of this
	//			EntityPersister#finishInitialization process is creating the
	//			entity's Attribute definitions.  As PluralAttribute definitions
	//			are recognized we create CollectionPersisters
	//
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public EntityPersister createEntityPersister(
			PersistentClass entityBinding,
			EntityRegionAccessStrategy entityCacheAccessStrategy,
			NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy,
			PersisterCreationContext creationContext) throws HibernateException {

		// todo : MappedSuperclass...

		// See if we had an existing (partially created) node...
		EntityHierarchyNode entityHierarchyNode = nameToHierarchyNodeMap.get( entityBinding.getEntityName() );
		if ( entityHierarchyNode != null ) {
			// we create the EntityHierarchyNode for all super types before we
			//		actually create the super's EntityPersister.  This check
			//		makes sure we do not have multiple call paths attempting to
			//		create the same EntityPersister multiple times
			assert entityHierarchyNode.ormJpaType == null;
		}
		else {
			entityHierarchyNode = new EntityHierarchyNode( entityBinding );
			nameToHierarchyNodeMap.put( entityBinding.getEntityName(), entityHierarchyNode );
		}

		final EntityPersister entityPersister = instantiateEntityPersister(
				entityBinding,
				entityCacheAccessStrategy,
				naturalIdCacheAccessStrategy,
				creationContext
		);

		creationContext.registerEntityPersister( entityPersister );

		entityHierarchyNode.ormJpaType = entityPersister;

		final EntityHierarchyNode superTypeNode = interpretSuperTypeNode( entityBinding );

		if ( superTypeNode == null ) {
			roots.add( entityHierarchyNode );
		}
		else {
			superTypeNode.addSubEntityNode( entityHierarchyNode );
		}

		return entityPersister;
	}

	@SuppressWarnings("unchecked")
	private <T> EntityPersister<T> instantiateEntityPersister(
			PersistentClass entityBinding,
			EntityRegionAccessStrategy entityCacheAccessStrategy,
			NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy,
			PersisterCreationContext creationContext) {
//		// If the metadata for the entity specified an explicit persister class, use it...
//		Class<? extends EntityPersister> persisterClass = entityBinding.getEntityPersisterClass();
//		if ( persisterClass == null ) {
//			// Otherwise, use the persister class indicated by the PersisterClassResolver service
//			persisterClass = serviceRegistry.getService( PersisterClassResolver.class ).getEntityPersisterClass( entityBinding );
//		}
//
//		return instantiateEntityPersister(
//				persisterClass,
//				entityBinding,
//				entityCacheAccessStrategy,
//				naturalIdCacheAccessStrategy,
//				creationContext
//		);

		EntityJavaTypeDescriptor<T> jtd = (EntityJavaTypeDescriptor<T>) creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry().getDescriptor( entityBinding.getEntityName() );
		if ( jtd == null ) {
			jtd = new EntityJavaTypeDescriptorImpl(
					entityBinding.getClassName(),
					entityBinding.getEntityName(),
					entityBinding.getMappedClass(),
					null,
					null,
					null
			);
			creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry().addDescriptor( jtd );
		}
		return new EntityPersisterImpl<>(
				entityBinding,
				null,
				null,
				creationContext
		);
	}

//	@SuppressWarnings( {"unchecked"})
//	private EntityPersister instantiateEntityPersister(
//			Class<? extends EntityPersister> persisterClass,
//			PersistentClass entityBinding,
//			EntityRegionAccessStrategy entityCacheAccessStrategy,
//			NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy,
//			PersisterCreationContext creationContext) {
//		try {
//			final Constructor<? extends EntityPersister> constructor = persisterClass.getConstructor( EntityPersister.CONSTRUCTOR_SIGNATURE );
//			try {
//				return constructor.newInstance(
//						entityBinding,
//						entityCacheAccessStrategy,
//						naturalIdCacheAccessStrategy,
//						creationContext
//				);
//			}
//			catch (MappingException e) {
//				throw e;
//			}
//			catch (InvocationTargetException e) {
//				Throwable target = e.getTargetException();
//				if ( target instanceof HibernateException ) {
//					throw (HibernateException) target;
//				}
//				else {
//					throw new MappingException( "Could not instantiate persister " + persisterClass.getName(), target );
//				}
//			}
//			catch (Exception e) {
//				throw new MappingException( "Could not instantiate persister " + persisterClass.getName(), e );
//			}
//		}
//		catch (MappingException e) {
//			throw e;
//		}
//		catch (Exception e) {
//			throw new MappingException( "Could not get constructor for " + persisterClass.getName(), e );
//		}
//	}
//
//	private InheritanceStrategy interpretInheritanceStrategy(PersistentClass entityBinding) {
//		if ( entityBinding instanceof RootClass ) {
//			if ( !entityBinding.hasSubclasses() ) {
//				return InheritanceStrategy.NONE;
//			}
//			return interpretInheritanceStrategy( (Subclass) entityBinding.getDirectSubclasses().next() );
//		}
//		else {
//			return interpretInheritanceStrategy( (Subclass) entityBinding );
//		}
//	}
//
//	private InheritanceStrategy interpretInheritanceStrategy(Subclass subEntityBinding) {
//		if ( subEntityBinding instanceof UnionSubclass ) {
//			return InheritanceStrategy.UNION;
//		}
//		else if ( subEntityBinding instanceof JoinedSubclass ) {
//			return InheritanceStrategy.JOINED;
//		}
//		else {
//			return InheritanceStrategy.DISCRIMINATOR;
//		}
//	}

	private EntityHierarchyNode interpretSuperTypeNode(PersistentClass entityBinding) {
		if ( entityBinding.getSuperMappedSuperclass() != null ) {
			// If entityBinding#getSuperMappedSuperclass() is not null, that is the direct super type
			return interpretMappedSuperclass( entityBinding.getSuperMappedSuperclass() );
		}
		else if ( entityBinding.getSuperclass() != null ) {
			// else, if entityBinding#getSuperclass() is not null, that is the direct super type
			// 		in this case we want to create the TypeHierarchyNode (if not already there), but not the persisters...
			// 		that will happen on later call to
			final String superTypeName = entityBinding.getSuperclass().getEntityName();
			EntityHierarchyNode node = nameToHierarchyNodeMap.computeIfAbsent(
					superTypeName,
					k -> new EntityHierarchyNode( entityBinding.getSuperclass() )
			);
			return node;
		}
		else {
			// else, there is no super.
			return null;
		}
	}

	private EntityHierarchyNode interpretMappedSuperclass(MappedSuperclass mappedSuperclass) {
		if ( mappedSuperclass == null ) {
			return null;
		}

		assert mappedSuperclass.getMappedClass() != null;
		final EntityHierarchyNode existing = nameToHierarchyNodeMap.get( mappedSuperclass.getMappedClass().getName() );
		if ( existing != null ) {
			return existing;
		}

		return makeMappedSuperclassTypeNode( mappedSuperclass );
	}

	private EntityHierarchyNode makeMappedSuperclassTypeNode(MappedSuperclass mappedSuperclass) {
		assert mappedSuperclass.getMappedClass() != null;

		final EntityHierarchyNode mappedSuperclassTypeSuperNode = interpretMappedSuperclass( mappedSuperclass.getSuperMappedSuperclass() );
		nameToHierarchyNodeMap.put(
				mappedSuperclass.getMappedClass().getName(),
				mappedSuperclassTypeSuperNode
		);

		return mappedSuperclassTypeSuperNode;
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public CollectionPersister createCollectionPersister(
			Collection collectionBinding,
			ManagedTypeImplementor source,
			String propertyName,
			CollectionRegionAccessStrategy cacheAccessStrategy,
			PersisterCreationContext creationContext) throws HibernateException {
//		// If the metadata for the collection specified an explicit persister class, use it
//		Class<? extends CollectionPersister> persisterClass = collectionBinding.getCollectionPersisterClass();
//		if ( persisterClass == null ) {
//			// Otherwise, use the persister class indicated by the PersisterClassResolver service
//			persisterClass = serviceRegistry.getService( PersisterClassResolver.class )
//					.getCollectionPersisterClass( collectionBinding );
//		}
//		return createCollectionPersister( persisterClass, collectionBinding, source, propertyName, cacheAccessStrategy, creationContext );

		// todo : ^^ we need to make this fit the published ctor contract (as much as possible)
		//		for now just create a simpl testing stub
		return new CollectionPersisterImpl(
				collectionBinding,
				source,
				propertyName,
				null,
				creationContext
		);
	}

	@SuppressWarnings( {"unchecked"})
	private CollectionPersister createCollectionPersister(
			Class<? extends CollectionPersister> persisterClass,
			Collection collectionBinding,
			ManagedTypeImplementor source,
			String propertyName,
			CollectionRegionAccessStrategy cacheAccessStrategy,
			PersisterCreationContext creationContext) {
		try {
			Constructor<? extends CollectionPersister> constructor = persisterClass.getConstructor( CollectionPersister.CONSTRUCTOR_SIGNATURE );
			try {
				return constructor.newInstance(
						collectionBinding,
						source,
						propertyName,
						cacheAccessStrategy,
						creationContext
				);
			}
			catch (MappingException e) {
				throw e;
			}
			catch (InvocationTargetException e) {
				Throwable target = e.getTargetException();
				if ( target instanceof HibernateException ) {
					throw (HibernateException) target;
				}
				else {
					throw new MappingException( "Could not instantiate collection persister " + persisterClass.getName(), target );
				}
			}
			catch (Exception e) {
				throw new MappingException( "Could not instantiate collection persister " + persisterClass.getName(), e );
			}
		}
		catch (MappingException e) {
			throw e;
		}
		catch (Exception e) {
			throw new MappingException( "Could not get constructor for " + persisterClass.getName(), e );
		}
	}

	@Override
	public EmbeddableMapper createEmbeddablePersister(
			Component componentBinding,
			CompositeContainer source,
			String localName,
			PersisterCreationContext creationContext) throws HibernateException {
		final EmbeddableMapperImpl mapper = new EmbeddableMapperImpl(
				creationContext,
				null,
				source,
				localName,
				componentBinding,
				null,
				null,
				null
		);
		embeddableComponentMap.put( mapper, componentBinding );
		mapper.setTypeConfiguration( creationContext.getTypeConfiguration() );
		return mapper;
	}

	@Override
	public void finishUp(PersisterCreationContext creationContext) {
		for ( EntityHierarchyNode root : roots ) {
			// todo : resolve any MappedSuperclasses for supers of the root entity
			EntityHierarchy entityHierarchy = new EntityHierarchyImpl(
					creationContext,
					(RootClass) root.mappingType,
					(EntityPersister) root.ormJpaType
			);

			finishSupers( root.superEntityNode, entityHierarchy, creationContext );

			root.finishUp( entityHierarchy, creationContext );

			entityHierarchy.finishInitialization( creationContext, (RootClass) root.mappingType );
		}

		// todo :
		for ( final Collection model : creationContext.getMetadata().getCollectionBindings() ) {
			final CollectionPersister collectionPersister = creationContext.getTypeConfiguration().findCollectionPersister( model.getRole() );
			if ( collectionPersister == null ) {
				throw new HibernateException( "Collection role not properly materialized to CollectionPersister : " + model.getRole() );
			}
			collectionPersister.finishInitialization( model, creationContext );
		}

		for ( EmbeddableMapper mapper : creationContext.getTypeConfiguration().getEmbeddablePersisters() ) {
			mapper.afterInitialization(
					embeddableComponentMap.get( mapper ),
					creationContext
			);
		}


		serviceRegistry = null;
		roots.clear();
		nameToHierarchyNodeMap.clear();
		embeddableComponentMap.clear();
	}

	private void finishSupers(EntityHierarchyNode node, EntityHierarchy hierarchy, PersisterCreationContext creationContext) {
		if ( node == null ) {
			return;
		}

		finishSupers( node.superEntityNode, hierarchy, creationContext );

		node.ormJpaType.finishInitialization( hierarchy, node.superEntityNode.ormJpaType, node.superEntityNode.mappingType, creationContext );
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Inner classes

	public static class EntityHierarchyNode {
		private final PersistentClass mappingType;
		private IdentifiableTypeImplementor ormJpaType;

		private EntityHierarchyNode superEntityNode;
		private Set<EntityHierarchyNode> subEntityNodes;

		public EntityHierarchyNode(PersistentClass mappingType) {
			this.mappingType = mappingType;
		}

		public PersistentClass getMappingType() {
			return mappingType;
		}

		public IdentifiableTypeImplementor getOrmJpaType() {
			return ormJpaType;
		}

		public void inject(IdentifiableTypeImplementor ormJpaType) {
			if ( this.ormJpaType != null ) {
				throw new IllegalStateException( "TypeHierarchyNode.ormJpaType ws already defined" );
			}
			this.ormJpaType = ormJpaType;
		}

		public void addSubEntityNode(EntityHierarchyNode subEntityNode) {
			if ( subEntityNodes == null ) {
				subEntityNodes = new HashSet<>();
			}
			subEntityNodes.add( subEntityNode );
			subEntityNode.setSuperEntityNode( this );
		}

		private void setSuperEntityNode(EntityHierarchyNode superEntityNode) {
			this.superEntityNode = superEntityNode;
		}

		public void finishUp(EntityHierarchy hierarchy, PersisterCreationContext creationContext) {
			if ( getOrmJpaType() == null ) {
				throw new HibernateException( "ORM-JPA IdentifiableTypeImplementor not yet known; cannot finishUp" );
			}


			final IdentifiableTypeImplementor superOrmJpaType = ( superEntityNode == null )
					? null
					: superEntityNode.getOrmJpaType();

			// todo : how many phases/passes do we want to give these IdentifiableTypeImplementor impls to finalize themselves?
			getOrmJpaType().finishInitialization( hierarchy, superOrmJpaType, mappingType, creationContext );

//			getEntityPersister().generateEntityDefinition();
//			getEntityPersister().postInstantiate();
//
//			// initialize the EntityPersister represented by this hierarchy node
//			getEntityPersister().finishInitialization( superType, entityBinding , creationContext );
//
			if ( getOrmJpaType() instanceof EntityPersister ) {
				creationContext.registerEntityNameResolvers( (EntityPersister) getOrmJpaType() );
			}

			if ( subEntityNodes != null ) {
				// pass finishUp processing to each of the sub-entity hierarchy nodes (recursive)
				for ( EntityHierarchyNode subTypeNode : subEntityNodes ) {
					subTypeNode.finishUp( hierarchy, creationContext );
				}
			}
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof EntityHierarchyNode ) ) {
				return false;
			}

			EntityHierarchyNode that = (EntityHierarchyNode) o;

			return mappingType.getEntityName().equals( that.mappingType.getEntityName() );

		}

		@Override
		public int hashCode() {
			return mappingType.getEntityName().hashCode();
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Deprecations

	/**
	 * @deprecated Use {@link org.hibernate.persister.internal.PersisterFactoryImpl#ENTITY_PERSISTER_CONSTRUCTOR_ARGS} instead.
	 */
	@Deprecated
	public static final Class[] ENTITY_PERSISTER_CONSTRUCTOR_ARGS = EntityPersister.CONSTRUCTOR_SIGNATURE;

	/**
	 * @deprecated Use {@link CollectionPersister#CONSTRUCTOR_SIGNATURE} instead
	 */
	@Deprecated
	public static final Class[] COLLECTION_PERSISTER_CONSTRUCTOR_ARGS = CollectionPersister.CONSTRUCTOR_SIGNATURE;
}
