/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import java.util.LinkedList;

/**
 * A general-purpose stack impl for use in parsing.
 *
 * @param <T> The type of things stored in the stack
 *
 * @author Steve Ebersole
 */
public class Stack<T> {
	private LinkedList<T> stack = new LinkedList<>();

	public void push(T newCurrent) {
		stack.addFirst( newCurrent );
	}

	public T pop() {
		return stack.removeFirst();
	}

	public T getCurrent() {
		return stack.peek();
	}
}
