/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class SubQuerySqmExpression implements SqmExpression {
	private final SqmQuerySpec querySpec;
	private final SqmExpressableType expressableType;

	public SubQuerySqmExpression(SqmQuerySpec querySpec, SqmExpressableType expressableType) {
		this.querySpec = querySpec;
		this.expressableType = expressableType;
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		return expressableType.getExportedDomainType();
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return expressableType;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	public SqmQuerySpec getQuerySpec() {
		return querySpec;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSubQueryExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<subquery>";
	}
}
