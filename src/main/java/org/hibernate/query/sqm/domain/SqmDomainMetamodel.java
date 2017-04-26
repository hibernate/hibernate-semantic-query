/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

import org.hibernate.persister.queryable.spi.BasicValuedExpressableType;
import org.hibernate.persister.queryable.spi.EntityValuedExpressableType;
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
	EntityValuedExpressableType resolveEntityReference(String entityName);
	<T> EntityValuedExpressableType<T> resolveEntityReference(Class<T> javaType);

	// - just push the cast target text into the tree.  let the consumer figure out how to interpret it?
	BasicValuedExpressableType resolveCastTargetType(String name);

	<T> BasicValuedExpressableType<T> resolveBasicType(Class<T> javaType);

	BasicValuedExpressableType resolveArithmeticType(
			BasicValuedExpressableType firstType,
			BasicValuedExpressableType secondType,
			BinaryArithmeticSqmExpression.Operation operation);

	BasicValuedExpressableType resolveSumFunctionType(BasicValuedExpressableType argumentType);
}
