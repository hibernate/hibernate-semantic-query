/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Navigable;

/**
 * @author Steve Ebersole
 */
public class LiteralNullSqmExpression implements LiteralSqmExpression<Void> {
	@Override
	public Void getLiteralValue() {
		return null;
	}

	@Override
	public BasicType getExpressionType() {
		return NULL_TYPE;
	}

	@Override
	public BasicType getInferableType() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Navigable type) {
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralNullExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<literal-null>";
	}

	private static BasicType NULL_TYPE = new BasicType() {
		@Override
		public Class getJavaType() {
			return void.class;
		}

		@Override
		public String asLoggableText() {
			return "NULL";
		}
	};
}
