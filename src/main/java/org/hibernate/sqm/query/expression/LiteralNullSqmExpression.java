/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class LiteralNullSqmExpression implements LiteralSqmExpression<Void> {
	private SqmExpressableType injectedExpressionType;

	public LiteralNullSqmExpression() {
		injectedExpressionType = NULL_TYPE;
	}

	@Override
	public Void getLiteralValue() {
		return null;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return injectedExpressionType;
	}

	@Override
	public SqmExpressableType getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLiteralNullExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<literal-null>";
	}

	private static SqmDomainTypeBasic NULL_TYPE = new SqmDomainTypeBasic() {
		@Override
		public SqmDomainTypeBasic getExportedDomainType() {
			return null;
		}

		@Override
		public Class getJavaType() {
			return void.class;
		}

		@Override
		public String asLoggableText() {
			return "NULL";
		}
	};

	@Override
	public SqmDomainType getExportedDomainType() {
		return injectedExpressionType.getExportedDomainType();
	}

	@Override
	public void impliedType(SqmExpressableType type) {
		injectedExpressionType = type;
	}
}
