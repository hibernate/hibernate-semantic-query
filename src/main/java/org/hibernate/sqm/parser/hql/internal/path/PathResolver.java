/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.hql.internal.path;

import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Strategy for resolving attribute path expressions in a contextually pluggable
 * manner.  Pluggable because how we resolve path expressions as part of a selection
 * is very different from how we need to resolve path expressions in predicates is
 * very different from how we need to resolve path expressions in from-clause ...
 *
 * @author Steve Ebersole
 */
public interface PathResolver {
	/**
	 * Resolve the given path.  Returns {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	DomainReferenceBinding resolvePath(String... pathParts);

	/**
	 * Resolve the given path relative to a given left-hand side.
	 *
	 * @param lhs The left-hand side
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	DomainReferenceBinding resolvePath(DomainReferenceBinding lhs, String... pathParts);

	/**
	 * Resolve the given path applying the specified "intrinsic" subclass indicator to the
	 * path terminal. Returns {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param subclassIndicator The "intrinsic" subclass indicator to apply to the path terminal.  See
	 * {@link SqmFrom#getIntrinsicSubclassIndicator()}
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	DomainReferenceBinding resolveTreatedPath(EntityReference subclassIndicator, String... pathParts);

	/**
	 * Resolve the given path relative to a given left-hand side applying the specified
	 * "intrinsic" subclass indicator to the path terminal
	 *
	 * @param subclassIndicator The "intrinsic" subclass indicator to apply to the path terminal.  See
	 * {@link SqmFrom#getIntrinsicSubclassIndicator()}
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	DomainReferenceBinding resolveTreatedPath(DomainReferenceBinding lhs, EntityReference subclassIndicator, String... pathParts);

	boolean canReuseImplicitJoins();
}
