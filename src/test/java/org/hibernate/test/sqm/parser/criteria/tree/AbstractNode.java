/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree;

import java.io.Serializable;

/**
 * All nodes in a criteria query tree will generally need access to the {@link CriteriaBuilderImpl} from which they
 * come.  This base class provides convenient, consistent support for that.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractNode implements Serializable {
	private final CriteriaBuilderImpl criteriaBuilder;

	public AbstractNode(CriteriaBuilderImpl criteriaBuilder) {
		this.criteriaBuilder = criteriaBuilder;
	}

	/**
	 * Provides access to the underlying {@link CriteriaBuilderImpl}.
	 *
	 * @return The underlying {@link CriteriaBuilderImpl} instance.
	 */
	public CriteriaBuilderImpl criteriaBuilder() {
		return criteriaBuilder;
	}
}
