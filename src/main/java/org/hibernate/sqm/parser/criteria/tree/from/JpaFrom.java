/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.from;

import javax.persistence.criteria.From;

import org.hibernate.sqm.parser.criteria.tree.path.JpaPath;

/**
 * @author Steve Ebersole
 */
public interface JpaFrom<Z,X> extends From<Z,X>, JpaPath<X> {
	//	FromImplementor<Z,X> correlateTo(CriteriaSubqueryImpl subquery);
	void prepareCorrelationDelegate(JpaFrom<Z, X> parent);
	JpaFrom<Z, X> getCorrelationParent();
}
