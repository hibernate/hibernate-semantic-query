/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.persister.queryable.spi.EntityValuedExpressableType;
import org.hibernate.query.sqm.domain.type.SqmDomainTypeEntity;

/**
 * Models a reference to an entity.
 *
 * @author Steve Ebersole
 *
 * @deprecated {@link EntityValuedExpressableType}
 */
@Deprecated
public interface SqmExpressableTypeEntity<T> extends SqmExpressableType<T>, SqmNavigableSource<T> {
	/**
	 * Obtain the name of the referenced entity
	 *
	 * @return The entity name
	 */
	String getEntityName();

	@Override
	SqmDomainTypeEntity<T> getExportedDomainType();
}
