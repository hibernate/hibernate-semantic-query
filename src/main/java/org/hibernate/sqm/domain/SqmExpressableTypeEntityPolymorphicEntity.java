/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import java.util.Set;

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
 * @see org.hibernate.sqm.QuerySplitter
 */
public interface SqmExpressableTypeEntityPolymorphicEntity extends SqmExpressableTypeEntity {
	/**
	 * Access to the specific "concrete" implementors of the
	 * unmapped polymorphism modeled here.
	 *
	 * @return All concrete implementors.
	 */
	Set<SqmExpressableTypeEntity> getImplementors();
}
