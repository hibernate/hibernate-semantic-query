/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.path.Binding;

/**
 * Strategy for resolving attribute path expressions in a contextually pluggable
 * manner.  Pluggable because how we resolve path expressions as part of a selection
 * is very different from how we need to resolve path expressions in predicates
 *
 * @author Steve Ebersole
 */
public interface PathResolver {
	/**
	 * Resolve the given path, returning {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param path The path tree to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	Binding resolvePath(HqlParser.DotIdentifierSequenceContext path);

	/**
	 * Resolve the given path, returning {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param path The path tree to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	Binding resolvePath(HqlParser.DotIdentifierSequenceContext path, EntityType subclassIndicator);
}
