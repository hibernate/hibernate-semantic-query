/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.AttributeReferenceSqmExpression;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final AttributeReferenceSqmExpression attributeReferenceExpression;

	public MemberOfSqmPredicate(AttributeReferenceSqmExpression attributeReferenceExpression) {
		this( attributeReferenceExpression, false );
	}

	public MemberOfSqmPredicate(AttributeReferenceSqmExpression attributeReferenceExpression, boolean negated) {
		super( negated );
		this.attributeReferenceExpression = attributeReferenceExpression;
	}

	public AttributeReferenceSqmExpression getAttributeReferenceExpression() {
		return attributeReferenceExpression;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
