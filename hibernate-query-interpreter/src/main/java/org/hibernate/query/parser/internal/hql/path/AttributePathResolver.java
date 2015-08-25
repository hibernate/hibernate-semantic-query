/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.hql.antlr.HqlParser;
import org.hibernate.sqm.path.AttributePathPart;

/**
 * Strategy for resolving attribute path expressions in a contextually pluggable manner.
 *
 * @author Steve Ebersole
 */
public interface AttributePathResolver {
	/**
	 * Resolve the given path, returning {@code null} if the initial parts do not indicate the
	 * path is an attribute path.
	 *
	 * @param path The path tree to resolve
	 *
	 * @return The resolve path, or {@code null}.
	 */
	AttributePathPart resolvePath(HqlParser.DotIdentifierSequenceContext path);
}
