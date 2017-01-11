/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainType;
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
	SqmExpressableTypeEntity resolveEntityReference(String entityName);
	SqmExpressableTypeEntity resolveEntityReference(Class javaType);

	SqmNavigable locateNavigable(SqmNavigableSource source, String navigableName);
	SqmNavigable resolveNavigable(SqmNavigableSource source, String navigableName) throws NoSuchAttributeException;

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	SqmExpressableTypeBasic resolveCastTargetType(String name);

	SqmExpressableTypeBasic resolveBasicType(Class javaType);

	SqmExpressableTypeBasic resolveArithmeticType(
			SqmExpressableTypeBasic firstType,
			SqmExpressableTypeBasic secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	SqmExpressableTypeBasic resolveSumFunctionType(SqmExpressableTypeBasic argumentType);

	SqmDomainType javaTypeToDomainType(Class javaType);
}
