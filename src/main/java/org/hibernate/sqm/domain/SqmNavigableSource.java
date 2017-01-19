/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain;

/**
 * Additional contract for SqmNavigable implementors that can in turn contain
 * SqmNavigable references.
 *
 * @author Steve Ebersole
 */
public interface SqmNavigableSource<J> extends SqmNavigable<J> {
	/**
	 * Find a contained SqmNavigable.  Returns {@code null} if the given
	 * "navigable name" cannot be resolved.
	 *
	 * @param navigableName The name to resolve relative to this source/container.
	 *
	 * @return The resolve navigable, or {@code null}
	 */
	SqmNavigable findNavigable(String navigableName);
}
