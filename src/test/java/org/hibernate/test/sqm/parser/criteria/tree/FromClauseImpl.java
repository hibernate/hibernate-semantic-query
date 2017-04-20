/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.query.sqm.produce.spi.criteria.from.JpaFrom;
import org.hibernate.query.sqm.produce.spi.criteria.from.JpaFromClause;
import org.hibernate.query.sqm.produce.spi.criteria.from.JpaRoot;

import org.hibernate.test.sqm.parser.criteria.tree.path.RootImpl;

/**
 * @author Steve Ebersole
 */
public class FromClauseImpl implements JpaFromClause {
	private LinkedHashSet<JpaRoot<?>> roots = new LinkedHashSet<>();
	private Set<JpaFrom<?,?>> correlationRoots;

	@Override
	public LinkedHashSet<JpaRoot<?>> getRoots() {
		return roots;
	}

	public <X> void addRoot(RootImpl<X> root) {
		roots.add( root );
	}
}
