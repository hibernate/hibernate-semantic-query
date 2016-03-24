/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.sqm.parser.criteria.spi.CriteriaVisitor;
import org.hibernate.sqm.parser.criteria.spi.expression.CriteriaExpression;
import org.hibernate.sqm.parser.criteria.spi.predicate.CriteriaPredicate;
import org.hibernate.sqm.parser.criteria.spi.predicate.NegatedCriteriaPredicate;
import org.hibernate.sqm.query.select.AliasedExpressionContainer;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractCriteriaExpressionImpl;

/**
 * @author Steve Ebersole
 */
public class NegatedPredicateWrapper extends AbstractCriteriaExpressionImpl<Boolean>
		implements PredicateImplementor, NegatedCriteriaPredicate, Serializable {
	private final PredicateImplementor predicate;
	private final BooleanOperator negatedOperator;
	private final List<Expression<Boolean>> negatedExpressions;

	@SuppressWarnings("unchecked")
	public NegatedPredicateWrapper(PredicateImplementor predicate) {
		super(
				predicate.criteriaBuilder(),
				( (CriteriaExpression) predicate ).getExpressionSqmType(),
				Boolean.class
		);
		this.predicate = predicate;
		this.negatedOperator = predicate.isJunction()
				? CompoundPredicate.reverseOperator( predicate.getOperator() )
				: predicate.getOperator();
		this.negatedExpressions = negateCompoundExpressions( predicate.getExpressions(), predicate.criteriaBuilder() );
	}

	private static List<Expression<Boolean>> negateCompoundExpressions(
			List<Expression<Boolean>> expressions,
			CriteriaBuilderImpl criteriaBuilder) {
		if ( expressions == null || expressions.isEmpty() ) {
			return Collections.emptyList();
		}

		final List<Expression<Boolean>> negatedExpressions = new ArrayList<Expression<Boolean>>();
		for ( Expression<Boolean> expression : expressions ) {
			if ( Predicate.class.isInstance( expression ) ) {
				negatedExpressions.add( ( (Predicate) expression ).not() );
			}
			else {
				negatedExpressions.add( criteriaBuilder.not( expression ) );
			}
		}
		return negatedExpressions;
	}

	@Override
	public BooleanOperator getOperator() {
		return negatedOperator;
	}

	@Override
	public boolean isJunction() {
		return predicate.isJunction();
	}

	@Override
	public boolean isNegated() {
		return ! predicate.isNegated();
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return negatedExpressions;
	}

	@Override
	public Predicate not() {
		return new NegatedPredicateWrapper( this );
	}

	@Override
	public org.hibernate.sqm.query.expression.Expression visitExpression(CriteriaVisitor visitor) {
		throw new UnsupportedOperationException( "Not expecting call to visitExpression on predicate" );
	}

	@Override
	public org.hibernate.sqm.query.predicate.Predicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitNegatedPredicate( this );
	}

	@Override
	public void visitSelections(CriteriaVisitor visitor, AliasedExpressionContainer container) {
		throw new UnsupportedOperationException( "Predicates cannot be used as select expression" );

	}

	@Override
	public CriteriaPredicate getPredicateToBeNegated() {
		return predicate;
	}
}
