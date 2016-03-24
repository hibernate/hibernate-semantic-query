/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import javax.persistence.TemporalType;

/**
 * Expose access to BasicTypes as well as access to EntityTypes by name
 *
 * @author Steve Ebersole
 */
public interface DomainMetamodel {
	<T> BasicType<T> getBasicType(Class<T> javaType);
	<T> BasicType<T> getBasicType(Class<T> javaType, TemporalType temporalType);

	EntityType resolveEntityType(Class javaType);
	EntityType resolveEntityType(String name);

	BasicType resolveCastTargetType(String name);
}
