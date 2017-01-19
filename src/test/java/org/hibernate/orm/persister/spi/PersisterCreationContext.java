/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.spi;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.common.spi.DatabaseModel;
import org.hibernate.orm.persister.embeddable.spi.EmbeddableMapper;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * "Parameter object" providing access to additional information that may be needed
 * in the creation of the persisters.
 *
 * @author Steve Ebersole
 */
public interface PersisterCreationContext {
	SessionFactoryImplementor getSessionFactory();

	TypeConfiguration getTypeConfiguration();
	MetadataImplementor getMetadata();
	DatabaseModel getDatabaseModel();

	PersisterFactory getPersisterFactory();

	void registerEntityPersister(EntityPersister entityPersister);
	void registerCollectionPersister(CollectionPersister collectionPersister);
	void registerEmbeddablePersister(EmbeddableMapper embeddableMapper);

	void registerEntityNameResolvers(EntityPersister entityPersister);
}
