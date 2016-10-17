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
public class TrimFunctionSqmExpression extends AbstractFunctionSqmExpression {
	public static final String NAME = "trim";

	public enum Specification {
		LEADING,
		TRAILING,
		BOTH
	}

	private final Specification specification;
	private final SqmExpression trimCharacter;
	private final SqmExpression source;

	public TrimFunctionSqmExpression(
			BasicType resultType,
			Specification specification,
			SqmExpression trimCharacter,
			SqmExpression source) {
		super( resultType );
		this.specification = specification;
		this.trimCharacter = trimCharacter;
		this.source = source;

		assert specification != null;
		assert trimCharacter != null;
		assert source != null;
	}

	@Override
	public String getFunctionName() {
		return NAME;
	}

	@Override
	public boolean hasArguments() {
		return true;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitTrimFunction( this );
	}

	@Override
	public String asLoggableText() {
		return "TRIM(" + specification.name() +
				" '" + trimCharacter.asLoggableText() +
				"' FROM " + source.asLoggableText() + ")";
	}
}
