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
public class SubstringFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "substr";

	private final SqmExpression source;
	private final SqmExpression startPosition;
	private final SqmExpression length;

	public SubstringFunctionSqmExpression(
			BasicType resultType,
			SqmExpression source,
			SqmExpression startPosition,
			SqmExpression length) {
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

	public SqmExpression getSource() {
		return source;
	}

	public SqmExpression getStartPosition() {
		return startPosition;
	}

	public SqmExpression getLength() {
		return length;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSubstringFunction( this );
	}

	@Override
	public String asLoggableText() {
		StringBuilder buff = new StringBuilder( "SUBSTR(" + getSource().asLoggableText() );

		if ( getStartPosition() != null ) {
			buff.append( ", " ).append( getStartPosition().asLoggableText() );
		}

		if ( getLength() != null ) {
			buff.append( ", " ).append( getLength().asLoggableText() );
		}

		return buff.append( ")" ).toString();
	}
}
