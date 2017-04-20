/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.common.internal.DatabaseModelImpl;
import org.hibernate.orm.persister.common.spi.DatabaseModel;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.persister.internal.PersisterFactoryImpl;
import org.hibernate.orm.persister.spi.PersisterCreationContext;
import org.hibernate.orm.persister.spi.PersisterFactory;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.query.sqm.domain.SqmDomainMetamodel;
import org.hibernate.query.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntityPolymorphicEntity;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.query.sqm.tree.expression.BinaryArithmeticSqmExpression;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ExplicitSqmDomainMetamodel implements SqmDomainMetamodel, PersisterCreationContext {
	private static final Logger log = Logger.getLogger( ExplicitSqmDomainMetamodel.class );

	private final TypeConfiguration typeConfiguration = new TypeConfiguration();
	private MetadataImplementor mappingMetadata;
	private final DatabaseModel databaseModel = new DatabaseModelImpl();

	private Map<String, String> importMap = new HashMap<>();

	private Map<String, SqmExpressableTypeEntityPolymorphicEntity> polymorphicEntityReferenceMap = new HashMap<>();

	public ExplicitSqmDomainMetamodel(MetadataImplementor mappingMetadata) {
		this.mappingMetadata = mappingMetadata;
	}

	@Override
	public SqmExpressableTypeEntity resolveEntityReference(String entityName) {
		if ( importMap.containsKey( entityName ) ) {
			entityName = importMap.get( entityName );
		}

		SqmExpressableTypeEntity entityType = typeConfiguration.findEntityPersister( entityName );
		if ( entityType == null ) {
			entityType = polymorphicEntityReferenceMap.get( entityName );
			if ( entityType == null ) {
				// see if it is an unmapped polymorphic entity reference
				entityType = resolveUnmappedPolymorphicReference( entityName );
			}
		}

		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec : no entity named " + entityName );
		}
		return entityType;
	}

	private SqmExpressableTypeEntity resolveUnmappedPolymorphicReference(String entityName) {
		// we have had a request to resolve a (supposed) entity-name into its `SqmExpressableTypeEntity`
		//		(EntityPersister) reference, but were not able to find a persister with exactly that name.
		//		Another option is that the query named a class (generally an interface) implemented
		//		bu one or more of the entities - see if we can find any

		final Class requestedClass = resolveRequestedClass( entityName );
		if ( requestedClass == null ) {
			return null;
		}

		// todo : explicit/implicit polymorphism...
		// todo : handle "duplicates" within a hierarchy

		final HashSet<EntityPersister> matchingPersisters = new HashSet<>();

		for ( EntityPersister checkPersister : typeConfiguration.getEntityPersisters() ) {
			if ( checkPersister.getJavaType() == null ) {
				continue;
			}

			if ( requestedClass.isAssignableFrom( checkPersister.getJavaType() ) ) {
				matchingPersisters.add( checkPersister );
			}
		}

		final PolymorphicEntityReferenceImpl entityReference = new PolymorphicEntityReferenceImpl(
				entityName,
				matchingPersisters
		);
		if ( entityReference != null ) {
			polymorphicEntityReferenceMap.put( entityName, entityReference );
		}
		return entityReference;
	}

	private Class resolveRequestedClass(String entityName) {
//		try {
//			return getSessionFactory().getServiceRegistry().getService( ClassLoaderService.class ).classForName( className );
//		}
//		catch (ClassLoadingException e) {
//			return null;
//		}
		try {
			return Class.forName( entityName );
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public <T> SqmExpressableTypeEntity<T> resolveEntityReference(Class<T> javaType) {
		final SqmExpressableTypeEntity entityType = typeConfiguration.findEntityPersister( javaType.getName() );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec" );
		}
		return entityType;
	}

	@Override
	public <T> BasicType<T> resolveBasicType(Class<T> javaType) {
		return typeConfiguration.getBasicTypeRegistry().getBasicType( javaType );
	}

	@Override
	public SqmExpressableTypeBasic resolveArithmeticType(
			SqmExpressableTypeBasic firstType,
			SqmExpressableTypeBasic secondType,
			BinaryArithmeticSqmExpression.Operation operation) {
		return ExpressionTypeHelper.resolveArithmeticType(
				firstType,
				secondType,
				operation == BinaryArithmeticSqmExpression.Operation.DIVIDE,
				this
		);
	}

	@Override
	public SqmExpressableTypeBasic resolveSumFunctionType(SqmExpressableTypeBasic argumentType) {
		return ExpressionTypeHelper.resolveSingleNumericType( argumentType, this );
	}

	@Override
	public SqmDomainTypeBasic resolveCastTargetType(String name) {
		return typeConfiguration.getBasicTypeRegistry().getBasicTypeForCast( name );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return null;
	}

	@Override
	public MetadataImplementor getMetadata() {
		return mappingMetadata;
	}

	@Override
	public DatabaseModel getDatabaseModel() {
		return databaseModel;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	private final PersisterFactoryImpl persisterFactory = new PersisterFactoryImpl();

	@Override
	public PersisterFactory getPersisterFactory() {
		return persisterFactory;
	}

	@Override
	public void registerEntityPersister(EntityPersister entityPersister) {
		typeConfiguration.register( entityPersister );
		importMap.put( entityPersister.getEntityName(), entityPersister.getEntityName() );
		importMap.put( entityPersister.getJpaEntityName(), entityPersister.getEntityName() );
		if ( entityPersister.getJavaTypeDescriptor().getJavaType() != null ) {
			importMap.put( entityPersister.getJavaTypeDescriptor().getJavaType().getSimpleName(), entityPersister.getEntityName() );
		}

		// todo : when moving upstream into MetamodelImpl, need stuff like:
//
//		if ( entityPersister.getConcreteProxyClass() != null
//				&& entityPersister.getConcreteProxyClass().isInterface()
//				&& !Map.class.isAssignableFrom( cp.getConcreteProxyClass() )
//				&& entityPersister.getMappedClass() != entityPersister.getConcreteProxyClass() ) {
//			// IMPL NOTE : we exclude Map based proxy interfaces here because that should
//			//		indicate MAP entity mode.0
//
//			if ( entityPersister.getMappedClass().equals( entityPersister.getConcreteProxyClass() ) ) {
//				// this part handles an odd case in the Hibernate test suite where we map an interface
//				// as the class and the proxy.  I cannot think of a real life use case for that
//				// specific test, but..
//				log.debugf( "Entity [%s] mapped same interface [%s] as class and proxy", cp.getEntityName(), cp.getMappedClass() );
//			}
//			else {
//				final String old = entityProxyInterfaceMap.put( entityPersister.getConcreteProxyClass(), entityPersister.getEntityName() );
//				if ( old != null ) {
//					throw new HibernateException(
//							String.format(
//									Locale.ENGLISH,
//									"Multiple entities [%s, %s] named the same interface [%s] as their proxy which is not supported",
//									old,
//									entityPersister.getEntityName(),
//									entityPersister.getConcreteProxyClass().getName()
//							)
//					);
//				}
//			}
//		}
	}

	@Override
	public void registerCollectionPersister(CollectionPersister collectionPersister) {
		typeConfiguration.register( collectionPersister );
	}

	@Override
	public void registerEmbeddablePersister(EmbeddableMapper embeddableMapper) {
		typeConfiguration.register( embeddableMapper );
	}

	@Override
	public void registerEntityNameResolvers(EntityPersister entityPersister) {

	}
}
