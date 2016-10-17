/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.parser.common.AttributeBinding;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final AttributeBinding attributeBinding;

	public MemberOfSqmPredicate(AttributeBinding attributeBinding) {
		this( attributeBinding, false );
	}

	public MemberOfSqmPredicate(AttributeBinding attributeBinding, boolean negated) {
		super( negated );
		this.attributeBinding = attributeBinding;
	}

	public AttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
