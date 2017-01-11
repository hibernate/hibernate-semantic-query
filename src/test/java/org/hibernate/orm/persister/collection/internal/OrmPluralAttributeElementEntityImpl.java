/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.collection.internal;

import java.util.List;

import org.hibernate.orm.persister.collection.spi.AbstractOrmPluralAttributeElement;
import org.hibernate.orm.persister.collection.spi.CollectionPersister;
import org.hibernate.orm.persister.collection.spi.OrmPluralAttributeElementEntity;
import org.hibernate.orm.persister.common.spi.Column;
import org.hibernate.orm.persister.entity.spi.EntityPersister;
import org.hibernate.orm.type.spi.EntityType;
import org.hibernate.sqm.domain.SqmNavigable;

/**
 * @author Steve Ebersole
 */
public class OrmPluralAttributeElementEntityImpl
		extends AbstractOrmPluralAttributeElement<EntityType>
		implements OrmPluralAttributeElementEntity {

	public OrmPluralAttributeElementEntityImpl(
			CollectionPersister persister,
			EntityType ormType, List<Column> columns) {
		super( persister, ormType, columns );
	}

	@Override
	public EntityPersister getEntityPersister() {
		return getOrmType().getEntityPersister();
	}

	@Override
	public SqmNavigable findNavigable(String navigableName) {
		return getEntityPersister().findNavigable( navigableName );
	}

	@Override
	public String getEntityName() {
		return getEntityPersister().getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getEntityPersister().getJpaEntityName();
	}

	@Override
	public ElementClassification getClassification() {
		return ElementClassification.ONE_TO_MANY;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.ENTITY;
	}

	@Override
	public String asLoggableText() {
		return "{entity-element}";
	}
}
