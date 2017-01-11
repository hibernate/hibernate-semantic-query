/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain.type;

import org.hibernate.sqm.domain.SqmExpressableTypeBasic;

/**
 * Models a basic type in the consumer's "type system",
 * <p/>
 * Hibernate requires basic types to be concrete types, aka not de-typed
 * (EntityMode.MAP) types.
 *
 * @author Steve Ebersole
 */
public interface SqmDomainTypeBasic extends SqmDomainType, SqmExpressableTypeBasic {
	/**
	 * Returns the Java type represented by this basic type.
	 * <p/>
	 * So long story short... this will always return an Optional where isPresent is true
	 */
	Class getJavaType();
}
