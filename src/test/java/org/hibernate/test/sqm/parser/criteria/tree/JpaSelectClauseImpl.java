/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import org.hibernate.query.sqm.produce.spi.criteria.select.JpaSelectClause;
import org.hibernate.query.sqm.produce.spi.criteria.select.JpaSelection;

/**
 * @author Steve Ebersole
 */
public class JpaSelectClauseImpl<T> implements JpaSelectClause<T> {
	private boolean distinct;
	private JpaSelection<? extends T> jpaSelection;

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	@Override
	public JpaSelection<? extends T> getSelection() {
		return jpaSelection;
	}

	public void setJpaSelection(JpaSelection<? extends T> jpaSelection) {
		this.jpaSelection = jpaSelection;
	}
}
