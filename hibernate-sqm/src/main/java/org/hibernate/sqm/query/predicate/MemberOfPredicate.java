/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
