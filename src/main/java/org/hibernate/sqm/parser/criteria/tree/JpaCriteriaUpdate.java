/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree;

import java.util.List;
import javax.persistence.criteria.CriteriaUpdate;

import org.hibernate.sqm.parser.criteria.tree.from.JpaRoot;

/**
 * @author Steve Ebersole
 */
public interface JpaCriteriaUpdate<E> extends CriteriaUpdate<E> {
	@Override
	JpaRoot<E> getRoot();

	List<JpaUpdateAssignment> getAssignments();

	@Override
	JpaPredicate getRestriction();
}
