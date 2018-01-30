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
	EntityDescriptor resolveEntityDescriptor(String entityName);
	EntityDescriptor resolveEntityDescriptor(Class javaType);

	AttributeDescriptor locateAttributeDescriptor(Navigable sourceBinding, String attributeName);
	AttributeDescriptor resolveAttributeDescriptor(Navigable sourceBinding, String attributeName) throws NoSuchAttributeException;

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	Navigable resolveCastTargetType(String name);

	BasicType resolveBasicType(Class javaType);

	BasicType resolveArithmeticType(
			Navigable firstType,
			Navigable secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	BasicType resolveSumFunctionType(Navigable argumentType);
}
