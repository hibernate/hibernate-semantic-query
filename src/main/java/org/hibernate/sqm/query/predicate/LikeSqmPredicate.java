/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.predicate;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.query.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class LikeSqmPredicate extends AbstractNegatableSqmPredicate {
	private final SqmExpression matchExpression;
	private final SqmExpression pattern;
	private final SqmExpression escapeCharacter;

	public LikeSqmPredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter) {
		this( matchExpression, pattern, escapeCharacter, false );
	}

	public LikeSqmPredicate(
			SqmExpression matchExpression,
			SqmExpression pattern,
			SqmExpression escapeCharacter, boolean negated) {
		super( negated );
		this.matchExpression = matchExpression;
		this.pattern = pattern;
		this.escapeCharacter = escapeCharacter;
	}

	public LikeSqmPredicate(SqmExpression matchExpression, SqmExpression pattern) {
		this( matchExpression, pattern, null );
	}

	public SqmExpression getMatchExpression() {
		return matchExpression;
	}

	public SqmExpression getPattern() {
		return pattern;
	}

	public SqmExpression getEscapeCharacter() {
		return escapeCharacter;
	}

	@Override
	public <T> T accept(SemanticQueryWalker<T> walker) {
		return walker.visitLikePredicate( this );
	}
}
