/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain.type;

import org.hibernate.sqm.domain.SqmLoggable;

/**
 * Represents any "type" used in the domain model.
 * <p/>
 * Think {@code org.hibernate.type.spi.Type} from Hibernate ORM.
 * <p/>
 * Mainly used to represent a singular memento we can embed into the SQM tree
 * for the consumer to be able to map back to its "type system"
 *
 * @author Steve Ebersole
 */
public interface SqmDomainType extends SqmLoggable {
	/**
	 * The underlying physical Java type.  May be {@code null}.
	 * <p/>
	 * Important to *not* define this as {@code Optional<Class>}.  Because the
	 * Java type is optional, it would seem "more natural" to define it as Optional,
	 * but we cannot.  The reason mainly because ORM binds this contract to the JPA
	 * {@link javax.persistence.metamodel.Type} contract which defines this
	 * same method name returning just {@code Class} (no Optional).
	 */
	Class getJavaType();
}
