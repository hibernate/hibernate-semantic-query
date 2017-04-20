/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.query.sqm.tree.expression.BinaryArithmeticSqmExpression;

/**
 * Exposes access back to the consumer to be able to resolve domain model
 * references encountered in the query.
 * <p/>
 * The entire premise of all these {@link org.hibernate.sqm.domain} contracts to
 *
 * @author Steve Ebersole
 *
 * @deprecated Move these to {@link org.hibernate.metamodel.spi.MetamodelImplementor}
 * or {@link org.hibernate.type.spi.TypeConfiguration}
 */
@Deprecated
public interface SqmDomainMetamodel {
	SqmExpressableTypeEntity resolveEntityReference(String entityName);
	<T> SqmExpressableTypeEntity<T> resolveEntityReference(Class<T> javaType);

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	SqmExpressableTypeBasic resolveCastTargetType(String name);

	<T> SqmExpressableTypeBasic<T> resolveBasicType(Class<T> javaType);

	SqmExpressableTypeBasic resolveArithmeticType(
			SqmExpressableTypeBasic firstType,
			SqmExpressableTypeBasic secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	SqmExpressableTypeBasic resolveSumFunctionType(SqmExpressableTypeBasic argumentType);
}
