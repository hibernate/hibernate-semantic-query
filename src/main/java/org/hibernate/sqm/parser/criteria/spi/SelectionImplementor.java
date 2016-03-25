/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi;

import javax.persistence.criteria.Selection;

import org.hibernate.sqm.query.select.AliasedSqmExpressionContainer;

/**
 * @author Steve Ebersole
 */
public interface SelectionImplementor<T> extends Selection<T> {
	void visitSelections(CriteriaVisitor visitor, AliasedSqmExpressionContainer container);
}
