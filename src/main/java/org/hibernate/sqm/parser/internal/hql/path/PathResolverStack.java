/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import java.util.LinkedList;

/**
 * @author Steve Ebersole
 */
public class PathResolverStack {
	private LinkedList<PathResolver> stack = new LinkedList<PathResolver>();

	public void push(PathResolver resolver) {
		stack.addFirst( resolver );
	}

	public PathResolver pop() {
		return stack.removeFirst();
	}

	public PathResolver getCurrent() {
		return stack.getFirst();
	}
}
