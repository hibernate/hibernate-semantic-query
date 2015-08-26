/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

import org.hibernate.sqm.SemanticQueryWalker;

/**
 * @author Steve Ebersole
 */
public interface Statement {
	enum Type {
		SELECT,
		INSERT,
		UPDATE,
		DELETE
	}

	Type getType();

	<T> T accept(SemanticQueryWalker<T> walker);
}
