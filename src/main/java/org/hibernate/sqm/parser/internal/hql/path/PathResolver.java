/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.path.Binding;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Strategy for resolving attribute path expressions in a contextually pluggable
 * manner.  Pluggable because how we resolve path expressions as part of a selection
 * is very different from how we need to resolve path expressions in predicates
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
	Binding resolvePath(String... pathParts);

	/**
	 * Resolve the given path relative to a given left-hand side.
	 *
	 * @param lhs The left-hand side
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	Binding resolvePath(AttributeBindingSource lhs, String... pathParts);

	/**
	 * Resolve the given path applying the specified "intrinsic" subclass indicator to the
	 * path terminal. Returns {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param subclassIndicator The "intrinsic" subclass indicator to apply to the path terminal.  See
	 * {@link FromElement#getIntrinsicSubclassIndicator()}
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	Binding resolvePath(EntityType subclassIndicator, String... pathParts);

	/**
	 * Resolve the given path relative to a given left-hand side applying the specified
	 * "intrinsic" subclass indicator to the path terminal
	 *
	 * @param subclassIndicator The "intrinsic" subclass indicator to apply to the path terminal.  See
	 * {@link FromElement#getIntrinsicSubclassIndicator()}
	 * @param pathParts The path parts to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	Binding resolvePath(AttributeBindingSource lhs, EntityType subclassIndicator, String... pathParts);

}
