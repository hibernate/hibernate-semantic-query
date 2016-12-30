/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.path;

import javax.persistence.criteria.Path;

import org.hibernate.sqm.parser.criteria.tree.JpaExpression;

/**
 * @author Steve Ebersole
 */
public interface JpaPath<X> extends Path<X>, JpaExpression<X> {
	@Override
	JpaPathSource<?> getParentPath();

	/**
	 * Defines handling for the JPA 2.1 TREAT down-casting feature.
	 *
	 * @param treatAsType The type to treat the path as.
	 * @param <T> The parameterized type representation of treatAsType.
	 *
	 * @return The properly typed view of this path.
	 */
	<T extends X> JpaPath<T> treatAs(Class<T> treatAsType);
}
