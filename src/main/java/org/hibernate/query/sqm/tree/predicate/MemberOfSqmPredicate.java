/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.predicate;

import org.hibernate.query.sqm.consume.spi.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeBinding;

/**
 * @author Steve Ebersole
 */
public class MemberOfSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmSingularAttributeBinding attributeBinding;

	public MemberOfSqmPredicate(SqmSingularAttributeBinding attributeBinding) {
		this( attributeBinding, false );
	}

	public MemberOfSqmPredicate(SqmSingularAttributeBinding attributeBinding, boolean negated) {
		super( negated );
		this.attributeBinding = attributeBinding;
	}

	public SqmSingularAttributeBinding getAttributeBinding() {
		return attributeBinding;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitMemberOfPredicate( this );
	}
}
