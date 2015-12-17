/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Represents something that can be bound into a query.  Typically this will be one of:<ul>
 *     <li>
 *         {@link ManagedType} - The entity/embeddable type itself is the {@link #getBoundType()} and
 *         the {@link #asManagedType()}
 *     </li>
 *     <li>
 *         {@link SingularAttribute} - The referenced attribute value is the {@link #getBoundType()}.
 *         {@link #asManagedType()} is safe to call if the attribute type is a managed type (entity, embeddable)
 *     </li>
 *     <li>
 *         {@link PluralAttribute} - The referenced attribute's element value is the {@link #getBoundType()}.
 *         {@link #asManagedType()} is safe to call if the element type is a managed type (entity, embeddable)
 *     </li>
 * </ul>
 * @author Steve Ebersole
 */
public interface Bindable {
	Type getBoundType();

	// if it can be further dereferenced
	ManagedType asManagedType();
}
