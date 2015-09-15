/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.query.predicate.Predicate;

import org.jboss.logging.Logger;

/**
 * Models a join based on a mapped attribute reference.
 *
 * @author Steve Ebersole
 */
public class QualifiedAttributeJoinFromElement
		extends AbstractJoinedFromElement
		implements QualifiedJoinedFromElement {
	private static final Logger log = Logger.getLogger( QualifiedAttributeJoinFromElement.class );

	private final String lhsAlias;
	private final AttributeDescriptor joinedAttributeDescriptor;
	private final boolean fetched;

	private Predicate onClausePredicate;

	public QualifiedAttributeJoinFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			String lhsAlias,
			AttributeDescriptor joinedAttributeDescriptor,
			JoinType joinType,
			boolean fetched) {
		super( fromElementSpace, alias, joinedAttributeDescriptor.getType(), joinType );
		this.lhsAlias = lhsAlias;
		this.joinedAttributeDescriptor = joinedAttributeDescriptor;
		this.fetched = fetched;
	}

	/**
	 * The FromElement alias for the "left hand side" of this join.
	 *
	 * @return The LHS FromElement alias.
	 */
	public String getLhsAlias() {
		return lhsAlias;
	}

	/**
	 * Obtain the descriptor for the attribute defining the join.
	 *
	 * @return The attribute descriptor
	 */
	public AttributeDescriptor getJoinedAttributeDescriptor() {
		return joinedAttributeDescriptor;
	}

	public boolean isFetched() {
		return fetched;
	}

	@Override
	public Predicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(Predicate predicate) {
		log.tracef(
				"Setting join predicate [%s] (was [%s])",
				predicate.toString(),
				this.onClausePredicate == null ? "<null>" : this.onClausePredicate.toString()
		);

		this.onClausePredicate = predicate;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitQualifiedAttributeJoinFromElement( this );
	}
}
