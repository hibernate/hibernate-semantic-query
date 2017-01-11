/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.type.SqmDomainTypeBasic;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * Represents an expression whose type is boolean, and can therefore be used as a predicate.
 *
 * @author Steve Ebersole
 */
public class BooleanExpressionSqmPredicate implements SqmPredicate {
	private final SqmExpression booleanExpression;

	public BooleanExpressionSqmPredicate(SqmExpression booleanExpression) {
		assert booleanExpression.getExpressionType() != null;
		assert booleanExpression.getExpressionType().getExportedDomainType() instanceof SqmDomainTypeBasic;
		final Class expressionJavaType = ( (SqmDomainTypeBasic) booleanExpression.getExpressionType() ).getJavaType();
		assert boolean.class.equals( expressionJavaType ) || Boolean.class.equals( expressionJavaType );

		this.booleanExpression = booleanExpression;
	}

	public SqmExpression getBooleanExpression() {
		return booleanExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitBooleanExpressionPredicate( this );
	}
}
