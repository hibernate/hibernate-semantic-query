/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.PolymorphicEntityDescriptor;

/**
 * EntityType extension for representing the "abstract schema type" in polymorphic
 * queries (e.g. {@code select o from java.lang.Object o}).
 *
 * @author Steve Ebersole
 */
public interface PolymorphicEntityType<T> extends EntityType, PolymorphicEntityDescriptor {
}
