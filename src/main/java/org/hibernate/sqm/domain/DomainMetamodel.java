/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;

/**
 * Exposes access back to the consumer to be able to resolve domain model
 * references encountered in the query.
 *
 * @author Steve Ebersole
 */
public interface DomainMetamodel {
	EntityReference resolveEntityReference(String entityName);
	EntityReference resolveEntityReference(Class javaType);

	AttributeReference locateAttributeReference(DomainReference sourceBinding, String attributeName);
	AttributeReference resolveAttributeReference(DomainReference sourceBinding, String attributeName) throws NoSuchAttributeException;

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	DomainReference resolveCastTargetType(String name);

	BasicType resolveBasicType(Class javaType);

	BasicType resolveArithmeticType(
			DomainReference firstType,
			DomainReference secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	BasicType resolveSumFunctionType(DomainReference argumentType);
}
