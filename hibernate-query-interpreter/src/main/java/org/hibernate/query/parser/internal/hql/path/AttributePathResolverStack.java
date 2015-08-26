/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import java.util.LinkedList;

/**
 * @author Steve Ebersole
 */
public class AttributePathResolverStack {
	private LinkedList<AttributePathResolver> stack = new LinkedList<AttributePathResolver>();

	public void push(AttributePathResolver resolver) {
		stack.addFirst( resolver );
	}

	public AttributePathResolver pop() {
		return stack.removeFirst();
	}

	public AttributePathResolver getCurrent() {
		return stack.getFirst();
	}
}
