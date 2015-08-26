/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
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
