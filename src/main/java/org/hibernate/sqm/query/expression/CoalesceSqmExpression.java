/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.DomainReference;

/**
 * @author Steve Ebersole
 */
public class CoalesceSqmExpression implements SqmExpression {
	private List<SqmExpression> values = new ArrayList<>();

	public List<SqmExpression> getValues() {
		return values;
	}

	public void value(SqmExpression expression) {
		values.add( expression );
	}

	@Override
	public DomainReference getExpressionType() {
		return values.get( 0 ).getExpressionType();
	}

	@Override
	public DomainReference getInferableType() {
		return getExpressionType();
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitCoalesceExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<coalesce>";
	}
}
