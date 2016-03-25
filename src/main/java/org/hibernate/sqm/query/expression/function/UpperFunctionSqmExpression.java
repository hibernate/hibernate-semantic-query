/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class UpperFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "upper";

	private SqmExpression expression;

	public UpperFunctionSqmExpression(BasicType resultType, SqmExpression expression) {
		super( resultType );
		this.expression = expression;

		assert expression != null;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	public SqmExpression getExpression() {
		return expression;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitUpperFunction( this );
	}
}
