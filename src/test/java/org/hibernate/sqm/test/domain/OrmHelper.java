/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.orm.persister.common.internal.DatabaseModelImpl;
import org.hibernate.orm.persister.common.spi.DerivedTable;
import org.hibernate.orm.persister.common.spi.PhysicalTable;
import org.hibernate.orm.persister.spi.PersisterFactory;
import org.hibernate.query.sqm.domain.SqmDomainMetamodel;

/**
 * @author Steve Ebersole
 */
public class OrmHelper {
	public static SqmDomainMetamodel buildDomainMetamodel(Class... managedClasses) {
		final MetadataSources metadataSources = new MetadataSources();
		for ( Class managedClass : managedClasses ) {
			metadataSources.addAnnotatedClass( managedClass );
		}

		final MetadataImplementor metadata = (MetadataImplementor) metadataSources.buildMetadata();
		metadata.validate();

		// at this point we have access to the mapping Metadata (PersistentClass, etc)
		//		use it to build the testing DomainMetamodel/TypeConfiguration

		final ExplicitSqmDomainMetamodel domainMetamodel = new ExplicitSqmDomainMetamodel( metadata );

		populateDatabaseModel( metadata, domainMetamodel );
		populateMappingModel( metadata, domainMetamodel );

		return domainMetamodel;
	}

	private static void populateMappingModel(MetadataImplementor mappingMetadata, ExplicitSqmDomainMetamodel domainMetamodel) {
//		final PersisterFactory persisterFactory = getSessionFactory().getServiceRegistry().getService( PersisterFactory.class );
		final PersisterFactory persisterFactory = domainMetamodel.getPersisterFactory();

		for ( final PersistentClass model : mappingMetadata.getEntityBindings() ) {
//			final EntityRegionAccessStrategy accessStrategy = getSessionFactory().getCache().determineEntityRegionAccessStrategy(
//					model
//			);
			final EntityRegionAccessStrategy accessStrategy = null;

//			final NaturalIdRegionAccessStrategy naturalIdAccessStrategy = getSessionFactory().getCache().determineNaturalIdRegionAccessStrategy(
//					model
//			);
			final NaturalIdRegionAccessStrategy naturalIdAccessStrategy = null;

			persisterFactory.createEntityPersister(
					model,
					accessStrategy,
					naturalIdAccessStrategy,
					domainMetamodel
			);
		}

		persisterFactory.finishUp( domainMetamodel );
	}

	private static void populateDatabaseModel(MetadataImplementor metadata, ExplicitSqmDomainMetamodel domainMetamodel) {
		final Database database = metadata.getDatabase();
		final DatabaseModelImpl databaseModel = (DatabaseModelImpl) domainMetamodel.getDatabaseModel();

		// todo : apply PhysicalNamingStrategy here, rather than as we create the "mapping model"?

		// todo : we need DatabaseModel to incorporate catalogs/schemas in some fashion
		//		either like org.hibernate.boot.model.relational.Database does
		//		or via catalogs/schemas-specific names
		for ( Namespace namespace : database.getNamespaces() ) {
			for ( Table mappingTable : namespace.getTables() ) {
				// todo : incorporate mapping Table's isAbstract indicator
				final org.hibernate.orm.persister.common.spi.Table table;
				if ( mappingTable instanceof DenormalizedTable ) {
					// this is akin to a UnionSubclassTable
					throw new NotYetImplementedException( "DenormalizedTable support not yet implemented" );
				}
				else if ( mappingTable.getSubselect() != null ) {
					table = new DerivedTable( mappingTable.getSubselect() );
				}
				else {
//					final JdbcEnvironment jdbcEnvironment = sessionFactory.getJdbcServices().getJdbcEnvironment();
//					final String qualifiedTableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
//							mappingTable.getQualifiedTableName(),
//							jdbcEnvironment.getDialect()
//					);
					final String qualifiedTableName = mappingTable.getQualifiedTableName().render();
					table = new PhysicalTable( qualifiedTableName );
				}

				databaseModel.registerTable( table );
			}
		}
	}

	private OrmHelper() {
	}
}
