/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class LikePredicate extends AbstractNegatablePredicate {
	private final Expression matchExpression;
	private final Expression pattern;
	private final Expression escapeCharacter;

	public LikePredicate(
			Expression matchExpression,
			Expression pattern,
			Expression escapeCharacter) {
		this( matchExpression, pattern, escapeCharacter, false );
	}

	public LikePredicate(
			Expression matchExpression,
			Expression pattern,
			Expression escapeCharacter, boolean negated) {
		super( negated );
		this.matchExpression = matchExpression;
		this.pattern = pattern;
		this.escapeCharacter = escapeCharacter;
	}

	public LikePredicate(Expression matchExpression, Expression pattern) {
		this( matchExpression, pattern, null );
	}

	public Expression getMatchExpression() {
		return matchExpression;
	}

	public Expression getPattern() {
		return pattern;
	}

	public Expression getEscapeCharacter() {
		return escapeCharacter;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLikePredicate( this );
	}
}
