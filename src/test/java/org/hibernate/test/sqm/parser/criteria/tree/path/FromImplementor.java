/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.path;
import javax.persistence.criteria.From;

/**
 * Implementation contract for the JPA {@link From} interface.
 *
 * @author Steve Ebersole
 */
public interface FromImplementor<Z,X> extends PathImplementor<X>, org.hibernate.sqm.parser.criteria.spi.path.FromImplementor<Z,X> {
//	FromImplementor<Z,X> correlateTo(CriteriaSubqueryImpl subquery);
	void prepareCorrelationDelegate(FromImplementor<Z, X> parent);
	FromImplementor<Z, X> getCorrelationParent();
}
