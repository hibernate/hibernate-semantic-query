/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class FunctionExpression implements Expression {
	private final String functionName;
	private final List<Expression> arguments;
	private final TypeDescriptor resultTypeDescriptor;

	public FunctionExpression(
			String functionName,
			List<Expression> arguments,
			TypeDescriptor resultTypeDescriptor) {
		this.functionName = functionName;
		this.arguments = arguments;
		this.resultTypeDescriptor = resultTypeDescriptor;
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return resultTypeDescriptor;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitFunctionExpression( this );
	}
}
