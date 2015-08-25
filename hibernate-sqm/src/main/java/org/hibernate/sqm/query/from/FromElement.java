/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import org.hibernate.sqm.SemanticQueryWalker;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;

/**
 * @author Steve Ebersole
 */
public interface FromElement extends AttributePathPart {
	FromElementSpace getContainingSpace();
	String getAlias();
	TypeDescriptor getTypeDescriptor();

	void addTreatedAs(TypeDescriptor typeDescriptor);

	<T> T accept(SemanticQueryWalker<T> walker);
}
