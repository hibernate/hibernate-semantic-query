/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.select;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * Represents the "object level view" of the query selection.  For a given query there
 * is just one Selection although that Selection might contain multiple "selection items".
 *
 * @author Steve Ebersole
 */
public interface Selection {
	<T> T accept(SemanticQueryWalker<T> walker);
}
