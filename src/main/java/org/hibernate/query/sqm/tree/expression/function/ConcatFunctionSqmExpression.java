/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.expression.function;

import java.util.List;

import org.hibernate.persister.queryable.spi.BasicValuedExpressableType;
import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.expression.ConcatSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * Differs from {@link ConcatSqmExpression} in that
 * the function can have multiple arguments, whereas ConcatExpression only has 2.
 *
 * @see ConcatSqmExpression
 *
 * @author Steve Ebersole
 */
public class ConcatFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "concat";

	private final List<SqmExpression> expressions;

	public ConcatFunctionSqmExpression(
			BasicValuedExpressableType resultType,
			List<SqmExpression> expressions) {
		super( resultType );
		this.expressions = expressions;

		assert expressions != null;
		assert expressions.size() >= 2;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	public List<SqmExpression> getExpressions() {
		return expressions;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitConcatFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "CONCAT(...)";
	}
}
