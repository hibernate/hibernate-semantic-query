/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;

/**
 * @author Steve Ebersole
 */
public class MemberOfPredicate implements NegatablePredicate {
	private final AttributeReferenceExpression attributeReferenceExpression;
	private final boolean negated;

	public MemberOfPredicate(AttributeReferenceExpression attributeReferenceExpression) {
		this( attributeReferenceExpression, false );
	}

	public MemberOfPredicate(AttributeReferenceExpression attributeReferenceExpression, boolean negated) {
		this.attributeReferenceExpression = attributeReferenceExpression;
		this.negated = negated;
	}

	public AttributeReferenceExpression getAttributeReferenceExpression() {
		return attributeReferenceExpression;
	}

	@Override
	public boolean isNegated() {
		return negated;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
