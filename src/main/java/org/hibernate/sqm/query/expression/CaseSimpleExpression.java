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

/**
 * @author Steve Ebersole
 */
public class CaseSimpleExpression implements Expression, ImpliedTypeExpression {
	private final Expression fixture;
	private List<WhenFragment> whenFragments = new ArrayList<WhenFragment>();
	private Expression otherwise;

	private Type type;
	private Type impliedType;

	public CaseSimpleExpression(Expression fixture) {
		this.fixture = fixture;
	}

	public Expression getFixture() {
		return fixture;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public Expression getOtherwise() {
		return otherwise;
	}

	public void otherwise(Expression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied type?
	}

	public void when(Expression test, Expression result) {
		whenFragments.add( new WhenFragment( test, result ) );
		// todo : inject implied type?
	}

	@Override
	public void impliedType(Type type) {
		this.impliedType = type;
		// todo : visit whenFragments and elseExpression
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
		return walker.visitSimpleCaseExpression( this );
	}

	public static class WhenFragment {
		private final Expression checkValue;
		private final Expression result;

		public WhenFragment(Expression checkValue, Expression result) {
			this.checkValue = checkValue;
			this.result = result;
		}

		public Expression getCheckValue() {
			return checkValue;
		}

		public Expression getResult() {
			return result;
		}
	}
}
