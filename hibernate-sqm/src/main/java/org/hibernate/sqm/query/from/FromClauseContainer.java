/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

/**
 * Unified contract for things that can contain a FromClause.
 *
 * @author Steve Ebersole
 */
public interface FromClauseContainer {
	/**
	 * Obtains this container's FromClause.
	 *
	 * @return This container's FromClause.
	 */
	FromClause getFromClause();
}
