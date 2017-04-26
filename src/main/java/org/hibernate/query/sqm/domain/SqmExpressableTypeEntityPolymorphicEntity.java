/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import java.util.Set;

import org.hibernate.persister.queryable.spi.EntityValuedExpressableType;
import org.hibernate.query.sqm.consume.spi.QuerySplitter;

/**
 * A specialized EntityReference for cases where the entity reference named
 * an "unmapped polymorphic" class.  E.g. given a query like
 * {@code select o from java.lang.Object o} we would ask the consumer for
 * an EntityReference for {@code java.lang.Object} and the consumer
 * would return us {@code PolymorphicEntityReference(java.lang.Object)}
 * which would contain all mapped entities as its {@link #getImplementors()}.
 * <p/>
 * Such "unmapped polymorphic" references are only valid in the root from-clause
 * and only one such reference is allow for the query.
 *
 * @author Steve Ebersole
 *
 * @see QuerySplitter
 *
 * @deprecated need a polymorphic form of {@link EntityValuedExpressableType}
 */
@Deprecated
public interface SqmExpressableTypeEntityPolymorphicEntity extends SqmExpressableTypeEntity {
	/**
	 * Access to the specific "concrete" implementors of the
	 * unmapped polymorphism modeled here.
	 *
	 * @return All concrete implementors.
	 */
	Set<SqmExpressableTypeEntity> getImplementors();
}
