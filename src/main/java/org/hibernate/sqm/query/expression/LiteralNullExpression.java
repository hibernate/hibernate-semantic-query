/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class LiteralNullExpression implements LiteralExpression<Void> {
	@Override
	public Void getLiteralValue() {
		return null;
	}

	@Override
	public BasicType<Void> getExpressionType() {
		return NULL_TYPE;
	}

	@Override
	public Type getInferableType() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void impliedType(Type type) {
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralNullExpression( this );
	}

	private static BasicType<Void> NULL_TYPE = new BasicType<Void>() {
		@Override
		public String getTypeName() {
			return void.class.getName();
		}

		@Override
		public Class<Void> getJavaType() {
			return void.class;
		}
	};
}
