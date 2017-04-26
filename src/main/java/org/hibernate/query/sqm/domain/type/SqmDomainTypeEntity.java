/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain.type;

import org.hibernate.type.spi.EntityType;

/**
 * Models an entity type in the consumer's "type system"
 *
 * @author Steve Ebersole
 *
 * @deprecated {@link EntityType}
 */
@Deprecated
public interface SqmDomainTypeEntity<T> extends SqmDomainType<T> {
}
