/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.domain.type.SqmDomainType;
import org.hibernate.query.sqm.domain.SqmExpressableType;

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
