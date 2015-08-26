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
public class MemberOfPredicate implements Predicate {
	private final AttributeReferenceExpression attributeReferenceExpression;

	public MemberOfPredicate(AttributeReferenceExpression attributeReferenceExpression) {
		this.attributeReferenceExpression = attributeReferenceExpression;
	}

	public AttributeReferenceExpression getAttributeReferenceExpression() {
		return attributeReferenceExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
