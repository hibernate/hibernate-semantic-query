/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree;

import java.util.List;

import org.hibernate.sqm.parser.criteria.tree.from.JpaFromClause;
import org.hibernate.sqm.parser.criteria.tree.select.JpaSelectClause;

/**
 * @author Steve Ebersole
 */
public interface JpaQuerySpec<T> {
	JpaSelectClause<T> getSelectClause();
	JpaFromClause getFromClause();
	JpaPredicate getRestriction();
	List<JpaOrder> getOrderList();
}
