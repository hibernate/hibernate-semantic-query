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
import org.hibernate.orm.type.descriptor.java.internal.NonStandardBasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.descriptor.java.spi.ImmutableMutabilityPlan;
import org.hibernate.orm.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.orm.type.internal.BasicTypeImpl;
import org.hibernate.orm.type.spi.TypeConfiguration;
import org.hibernate.sqm.domain.SqmDomainMetamodel;
import org.hibernate.sqm.domain.NavigableResolutionException;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.domain.SqmExpressableTypeEntity;
import org.hibernate.sqm.domain.SqmExpressableTypeEntityPolymorphicEntity;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.domain.SqmNavigableSource;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;

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
	private Map<String,SqmDomainType> javaTypeNameToDomainTypeMap = new HashMap<>();

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
	public SqmExpressableTypeEntity resolveEntityReference(Class javaType) {
		final SqmExpressableTypeEntity entityType = typeConfiguration.findEntityPersister( javaType.getName() );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "Per JPA spec" );
		}
		return entityType;
	}

	@Override
	public SqmNavigable locateNavigable(SqmNavigableSource source, String navigableName) {
		return source.findNavigable( navigableName );
	}

	@Override
	public SqmNavigable resolveNavigable(SqmNavigableSource source, String navigableName) {
		final SqmNavigable navigable = locateNavigable( source, navigableName );
		if ( navigable == null ) {
			throw new NavigableResolutionException( "Could not locate attribute named [" + navigableName + " relative to [" + source.asLoggableText() + "]" );
		}
		return navigable;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SqmDomainTypeBasic resolveBasicType(Class javaType) {
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
	public SqmDomainType javaTypeToDomainType(Class javaType) {
		SqmDomainType domainType = javaTypeNameToDomainTypeMap.get( javaType.getName() );
		if ( domainType == null ) {
			JavaTypeDescriptor javaTypeDescriptor = typeConfiguration.getJavaTypeDescriptorRegistry()
					.getDescriptor( javaType );
			// assume it is a BasicDomainType
			if ( javaTypeDescriptor == null ) {
				domainType = new NonStandardBasicJavaTypeDescriptor( javaType );
				typeConfiguration.getJavaTypeDescriptorRegistry().addDescriptor( (JavaTypeDescriptor) domainType );
				javaTypeNameToDomainTypeMap.put( javaType.getName(), domainType );
				return domainType;
			}
			else {
				if ( javaTypeDescriptor instanceof SqmDomainType ) {
					javaTypeNameToDomainTypeMap.put( javaType.getName(), domainType );
					return (SqmDomainType) javaTypeDescriptor;
				}
				domainType = new BasicTypeImpl(
						javaType.getName(),
						(BasicJavaTypeDescriptor) javaTypeDescriptor,
						// assume immutable
						ImmutableMutabilityPlan.INSTANCE,
						// assume no Comparator
						null,
						javaTypeDescriptor.getJdbcRecommendedSqlType(
								typeConfiguration.getBasicTypeRegistry().getBaseJdbcRecommendedSqlTypeMappingContext()
						)
				);
				javaTypeNameToDomainTypeMap.put( javaType.getName(), domainType );
			}
		}
		return domainType;
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
