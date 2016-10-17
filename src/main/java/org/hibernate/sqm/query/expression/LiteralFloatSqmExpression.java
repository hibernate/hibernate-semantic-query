/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class LiteralFloatSqmExpression extends AbstractLiteralSqmExpressionImpl<Float> {
	public LiteralFloatSqmExpression(Float value) {
		super( value );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralFloatExpression( this );
	}
}
