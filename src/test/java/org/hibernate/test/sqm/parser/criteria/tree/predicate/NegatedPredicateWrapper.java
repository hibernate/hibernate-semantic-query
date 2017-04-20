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
import java.util.stream.Collectors;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.query.sqm.ParsingException;
import org.hibernate.query.sqm.produce.spi.criteria.CriteriaVisitor;
import org.hibernate.query.sqm.produce.spi.criteria.JpaPredicate;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractJpaExpressionImpl;

/**
 * @author Steve Ebersole
 */
public class NegatedPredicateWrapper
		extends AbstractJpaExpressionImpl<Boolean>
		implements JpaPredicate, Serializable {
	private final JpaPredicate predicate;
	private final BooleanOperator negatedOperator;
	private final List<JpaPredicate> negatedPredicates;

	@SuppressWarnings("unchecked")
	public NegatedPredicateWrapper(JpaPredicate predicate) {
		super(
				(CriteriaBuilderImpl) predicate.criteriaBuilder(),
				null,
				Boolean.class
		);
		this.predicate = predicate;
		this.negatedOperator = predicate.getOperator() == BooleanOperator.AND
				? CompoundPredicate.reverseOperator( predicate.getOperator() )
				: predicate.getOperator();
		this.negatedPredicates = negateCompoundExpressions(
				predicate.getExpressions(),
				(CriteriaBuilderImpl) predicate.criteriaBuilder()
		);
	}

	private static List<JpaPredicate> negateCompoundExpressions(
			List<Expression<Boolean>> expressions,
			CriteriaBuilderImpl criteriaBuilder) {
		if ( expressions == null || expressions.isEmpty() ) {
			return Collections.emptyList();
		}

		final List<JpaPredicate> negatedExpressions = new ArrayList<>();
		for ( Expression<Boolean> expression : expressions ) {
			if ( Predicate.class.isInstance( expression ) ) {
				negatedExpressions.add( ( (JpaPredicate) expression ).not() );
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
	public boolean isNegated() {
		return !predicate.isNegated();
	}

	@Override
	public List<Expression<Boolean>> getExpressions() {
		return negatedPredicates.stream().collect( Collectors.toList() );
	}

	@Override
	public JpaPredicate not() {
		return new NegatedPredicateWrapper( this );
	}

	@Override
	public SqmPredicate visitPredicate(CriteriaVisitor visitor) {
		return visitor.visitNegatedPredicate( this );
	}

	@Override
	public SqmExpression visitExpression(CriteriaVisitor visitor) {
		throw new ParsingException( "Unexpected call to visitExpression on JpaPredicate" );
	}
}
