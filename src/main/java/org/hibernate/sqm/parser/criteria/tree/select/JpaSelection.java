/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.select;

import javax.persistence.criteria.Selection;

import org.hibernate.sqm.parser.criteria.tree.CriteriaVisitor;
import org.hibernate.sqm.query.select.SqmAliasedExpressionContainer;

/**
 * @author Steve Ebersole
 */
public interface JpaSelection<T> extends Selection<T> {
	void visitSelections(CriteriaVisitor visitor, SqmAliasedExpressionContainer container);
}
