/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.hql;

import org.hibernate.query.parser.internal.ParsingContext;

/**
 * @author Steve Ebersole
 */
class ParsingContextTestingImpl extends ParsingContext {

	public ParsingContextTestingImpl() {
		super( new ConsumerContextTestingImpl() );
	}
}
