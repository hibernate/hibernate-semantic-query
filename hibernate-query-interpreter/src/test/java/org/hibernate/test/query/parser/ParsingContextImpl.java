/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.query.parser;

import org.hibernate.query.parser.internal.ParsingContext;

/**
 * @author Steve Ebersole
 */
public class ParsingContextImpl extends ParsingContext {
	public ParsingContextImpl() {
		super( new ConsumerContextImpl() );
	}
}
