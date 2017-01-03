/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.persister.common.domain.spi;

import org.hibernate.sqm.domain.SqmNavigable;

/**
 * Stand-in for a new proposed contract in the evolving Hibernate
 * type/persister/domain-model model representing reference to any of:<ul>
 *     <li>entity</li>
 *     <li>composite/embeddable</li>
 *     <li>attributes</li>
 *     <li>
 *         collection elements ({@code VALUE(aPersistentMapValuedAttribute)} /
 *         {@code ELEMENTS(anyPersistentCollectionAttribute)})
 *     </li>
 *     <li>
 *         collection indices ({@code KEY(aPersistentMapValuedAttribute)} /
 *         {@code INDEX(anyIndexedPersistentCollectionAttribute)})
 *     </li>
 * </ul>
 *
 * @see NavigableSource
 *
 * @author Steve Ebersole
 */
public interface Navigable extends SqmNavigable {
	/**
	 * Returns the "left hand side" of this Navigable.  In the case of a
	 * "root" reference, this will return {@code null}.
	 */
	NavigableSource getSource();

	/**
	 * The name of this Navigable relative to its NavigableSource.
	 */
	String getNavigableName();
}
