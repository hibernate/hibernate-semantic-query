/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.query.SqmQuerySpec;

/**
 * @author Steve Ebersole
 */
public class SubQuerySqmExpression implements SqmExpression {
	private final SqmQuerySpec querySpec;
	private final DomainReference type;

	public SubQuerySqmExpression(SqmQuerySpec querySpec, DomainReference type) {
		this.querySpec = querySpec;
		this.type = type;
	}

	@Override
	public DomainReference getExpressionType() {
		return type;
	}

	@Override
	public DomainReference getInferableType() {
		return type;
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
