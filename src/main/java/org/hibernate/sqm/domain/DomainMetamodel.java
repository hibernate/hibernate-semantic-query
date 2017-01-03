/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.domain.type.BasicDomainType;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;

/**
 * Exposes access back to the consumer to be able to resolve domain model
 * references encountered in the query.
 * <p/>
 * The entire premise of all these {@link org.hibernate.sqm.domain} contracts to
 *
 * @author Steve Ebersole
 */
public interface DomainMetamodel {
	EntityReference resolveEntityReference(String entityName);
	EntityReference resolveEntityReference(Class javaType);

	SqmNavigable locateNavigable(SqmNavigableSource source, String navigableName);
	SqmNavigable resolveNavigable(SqmNavigableSource source, String navigableName) throws NoSuchAttributeException;

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	BasicDomainType resolveCastTargetType(String name);

	BasicDomainType resolveBasicType(Class javaType);

	BasicDomainType resolveArithmeticType(
			DomainReference firstType,
			DomainReference secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	BasicDomainType resolveSumFunctionType(DomainReference argumentType);
}
