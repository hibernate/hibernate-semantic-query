/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;

/**
 * @author Steve Ebersole
 */
public class LiteralFalseSqmExpression extends AbstractLiteralSqmExpressionImpl<Boolean> {
	public LiteralFalseSqmExpression(SqmExpressableTypeBasic expressionType) {
		super( Boolean.FALSE, expressionType );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralFalseExpression( this );
	}
}
