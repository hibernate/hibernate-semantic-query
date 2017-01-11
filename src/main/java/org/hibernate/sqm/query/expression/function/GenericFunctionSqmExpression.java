/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class GenericFunctionSqmExpression extends AbstractFunctionSqmExpression implements SqmExpression {
	private final String functionName;
	private final List<SqmExpression> arguments;

	public GenericFunctionSqmExpression(
			String functionName,
			SqmExpressableTypeBasic resultType,
			List<SqmExpression> arguments) {
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

	public List<SqmExpression> getArguments() {
		return arguments;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitGenericFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "function(" + getFunctionName() + " ...)";
	}
}
