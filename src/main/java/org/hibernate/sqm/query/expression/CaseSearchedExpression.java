/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.expression;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.predicate.Predicate;

/**
 * @author Steve Ebersole
 */
public class CaseSearchedExpression implements Expression, ImpliedTypeExpression {
	private List<WhenFragment> whenFragments = new ArrayList<WhenFragment>();
	private Expression otherwise;

	private Type type;
	private Type impliedType;

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public Expression getOtherwise() {
		return otherwise;
	}

	public void when(Predicate predicate, Expression result) {
		whenFragments.add( new WhenFragment( predicate, result ) );
	}

	public void otherwise(Expression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied type?
	}
	@Override
	public void impliedType(Type type) {
		this.impliedType = type;
		// todo : visit whenFragments and otherwise
	}

	@Override
	public Type getExpressionType() {
		if ( impliedType != null ) {
			return impliedType;
		}

		if ( otherwise != null ) {
			return otherwise.getExpressionType();
		}

		for ( WhenFragment whenFragment : whenFragments ) {
			if ( whenFragment.result.getExpressionType() != null ) {
				return whenFragment.result.getExpressionType();
			}
		}

		return null;
	}

	@Override
	public Type getInferableType() {
		if ( otherwise != null ) {
			return otherwise.getInferableType();
		}

		for ( WhenFragment whenFragment : whenFragments ) {
			if ( whenFragment.result.getExpressionType() != null ) {
				return whenFragment.result.getInferableType();
			}
		}

		return null;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSearchedCaseExpression( this );
	}

	public static class WhenFragment {
		private final Predicate predicate;
		private final Expression result;

		public WhenFragment(Predicate predicate, Expression result) {
			this.predicate = predicate;
			this.result = result;
		}

		public Predicate getPredicate() {
			return predicate;
		}

		public Expression getResult() {
			return result;
		}
	}
}
