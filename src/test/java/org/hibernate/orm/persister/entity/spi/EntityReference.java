/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.entity.spi;

import org.hibernate.orm.persister.common.spi.OrmNavigable;
import org.hibernate.query.sqm.domain.SqmExpressableTypeEntity;

/**
 * @author Steve Ebersole
 */
public interface EntityReference<T> extends OrmNavigable<T>, SqmExpressableTypeEntity<T> {
	EntityPersister<T> getEntityPersister();

	String getTypeName();
	String getEntityName();
	String getJpaEntityName();
}
