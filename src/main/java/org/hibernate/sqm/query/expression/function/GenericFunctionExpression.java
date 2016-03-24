/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class GenericFunctionExpression extends AbstractFunctionExpression implements Expression {
	private final String functionName;
	private final List<Expression> arguments;

	public GenericFunctionExpression(
			String functionName,
			BasicType resultType,
			List<Expression> arguments) {
		super( resultType );
		this.functionName = functionName;
		this.arguments = arguments;
	}

	public String getFunctionName() {
		return functionName;
	}

	@Override
	public boolean hasArguments() {
		return arguments != null && !arguments.isEmpty();
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGenericFunction( this );
	}
}
