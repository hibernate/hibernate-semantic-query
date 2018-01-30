/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.domain.SingularAttributeReference;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SingularAttributeReference attributeBinding;

	public MemberOfSqmPredicate(SingularAttributeReference attributeBinding) {
		this( attributeBinding, false );
	}

	public MemberOfSqmPredicate(SingularAttributeReference attributeBinding, boolean negated) {
		super( negated );
		this.attributeBinding = attributeBinding;
	}

	public SingularAttributeReference getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
