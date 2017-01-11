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
import org.hibernate.sqm.domain.type.SqmDomainType;
import org.hibernate.sqm.domain.SqmExpressableType;

/**
 * @author Steve Ebersole
 */
public class CaseSimpleSqmExpression implements SqmExpression, ImpliedTypeSqmExpression {
	private final SqmExpression fixture;
	private List<WhenFragment> whenFragments = new ArrayList<>();
	private SqmExpression otherwise;

	private SqmExpressableType expressableType;
	private SqmExpressableType impliedType;

	public CaseSimpleSqmExpression(SqmExpression fixture) {
		this.fixture = fixture;
	}

	public SqmExpression getFixture() {
		return fixture;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public SqmExpression getOtherwise() {
		return otherwise;
	}

	public void otherwise(SqmExpression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied expressableType?
	}

	public void when(SqmExpression test, SqmExpression result) {
		whenFragments.add( new WhenFragment( test, result ) );
		// todo : inject implied expressableType?
	}

	@Override
	public void impliedType(SqmExpressableType type) {
		this.impliedType = type;
		// todo : visit whenFragments and elseExpression
	}

	@Override
	public SqmDomainType getExportedDomainType() {
		if ( impliedType != null ) {
			return impliedType.getExportedDomainType();
		}

		if ( otherwise != null ) {
			return otherwise.getExpressionType().getExportedDomainType();
		}

		for ( WhenFragment whenFragment : whenFragments ) {
			if ( whenFragment.result.getExpressionType() != null ) {
				return whenFragment.result.getExpressionType().getExportedDomainType();
			}
		}

		return null;
	}

	@Override
	public SqmExpressableType getExpressionType() {
		return expressableType;
	}

	@Override
	public SqmExpressableType getInferableType() {
		if ( otherwise != null ) {
			return otherwise.getInferableType();
		}

		for ( WhenFragment whenFragment : whenFragments ) {
			if ( whenFragment.result.getExpressionType() != null ) {
				return whenFragment.result.getInferableType();
			}
		}

		return expressableType;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitSimpleCaseExpression( this );
	}

	@Override
	public String asLoggableText() {
		return "<simple-case>";
	}

	public static class WhenFragment {
		private final SqmExpression checkValue;
		private final SqmExpression result;

		public WhenFragment(SqmExpression checkValue, SqmExpression result) {
			this.checkValue = checkValue;
			this.result = result;
		}

		public SqmExpression getCheckValue() {
			return checkValue;
		}

		public SqmExpression getResult() {
			return result;
		}
	}
}
