/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression.function;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class SubstringFunctionExpression extends AbstractFunctionExpression  {
	public static final String NAME = "substr";

	private final Expression source;
	private final Expression startPosition;
	private final Expression length;

	public SubstringFunctionExpression(
			BasicType resultType,
			Expression source,
			Expression startPosition,
			Expression length) {
		super( resultType );
		this.source = source;
		this.startPosition = startPosition;
		this.length = length;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	public Expression getSource() {
		return source;
	}

	public Expression getStartPosition() {
		return startPosition;
	}

	public Expression getLength() {
		return length;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSubstringFunction( this );
	}
}
