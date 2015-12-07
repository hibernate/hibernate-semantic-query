/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class FunctionExpression implements Expression {
	private final String functionName;
	private final List<Expression> arguments;
	private final BasicType resultTypeDescriptor;

	public FunctionExpression(
			String functionName,
			BasicType resultTypeDescriptor,
			List<Expression> arguments) {
		this.functionName = functionName;
		this.resultTypeDescriptor = resultTypeDescriptor;
		this.arguments = arguments;
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public BasicType getExpressionType() {
		return resultTypeDescriptor;
	}

	@Override
	public Type getInferableType() {
		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitFunctionExpression( this );
	}
}
