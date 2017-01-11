/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.math.BigDecimal;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.SqmExpressableTypeBasic;

/**
 * @author Steve Ebersole
 */
public class LiteralBigDecimalSqmExpression extends AbstractLiteralSqmExpressionImpl<BigDecimal> {
	public LiteralBigDecimalSqmExpression(BigDecimal bigDecimal, SqmExpressableTypeBasic sqmExpressableTypeBasic) {
		super( bigDecimal, sqmExpressableTypeBasic );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralBigDecimalExpression( this );
	}
}
