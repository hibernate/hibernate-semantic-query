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
public class LiteralCharacterSqmExpression extends AbstractLiteralSqmExpressionImpl<Character> {
	public LiteralCharacterSqmExpression(Character value) {
		super( value );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralCharacterExpression( this );
	}
}
