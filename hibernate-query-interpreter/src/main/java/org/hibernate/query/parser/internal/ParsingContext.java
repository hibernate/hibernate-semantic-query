/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal;

import org.hibernate.query.parser.ConsumerContext;

/**
 * Represents contextual information for each parse
 *
 * @author Steve Ebersole
 */
public class ParsingContext {
	private final ConsumerContext consumerContext;
	private final ImplicitAliasGenerator aliasGenerator = new ImplicitAliasGenerator();
	private final AliasRegistry aliasRegistry = new AliasRegistry();

	public ParsingContext(ConsumerContext consumerContext) {
		this.consumerContext = consumerContext;
	}

	public ConsumerContext getConsumerContext() {
		return consumerContext;
	}

	public ImplicitAliasGenerator getImplicitAliasGenerator() {
		return aliasGenerator;
	}

	public AliasRegistry getAliasRegistry() {
		return aliasRegistry;
	}
}
