/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SingularAttributeBinding attributeBinding;

	public MemberOfSqmPredicate(SingularAttributeBinding attributeBinding) {
		this( attributeBinding, false );
	}

	public MemberOfSqmPredicate(SingularAttributeBinding attributeBinding, boolean negated) {
		super( negated );
		this.attributeBinding = attributeBinding;
	}

	public SingularAttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
