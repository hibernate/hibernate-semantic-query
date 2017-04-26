/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression;

import java.math.BigDecimal;

import org.hibernate.persister.queryable.spi.BasicValuedExpressableType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public class LiteralBigDecimalSqmExpression extends AbstractLiteralSqmExpressionImpl<BigDecimal> {
	public LiteralBigDecimalSqmExpression(BigDecimal bigDecimal, BasicValuedExpressableType sqmExpressableTypeBasic) {
		super( bigDecimal, sqmExpressableTypeBasic );
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralBigDecimalExpression( this );
	}
}
