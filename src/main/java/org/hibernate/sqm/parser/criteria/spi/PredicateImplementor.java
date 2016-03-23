/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi;

import javax.persistence.criteria.Predicate;

/**
 * @author Steve Ebersole
 */
public interface PredicateImplementor extends Predicate {
	org.hibernate.sqm.query.predicate.Predicate visitPredicate(CriteriaVisitor visitor);
}
