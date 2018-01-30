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
	private LinkedList<T> internalStack = new LinkedList<>();

	public Stack() {
	}

	public Stack(T initialValue) {
		push( initialValue );
	}

	public void push(T newCurrent) {
		internalStack.addFirst( newCurrent );
	}

	public T pop() {
		return internalStack.removeFirst();
	}

	public T getCurrent() {
		return internalStack.peek();
	}

	public T getPrevious() {
		if ( internalStack.size() < 2 ) {
			return null;
		}
		return internalStack.get( internalStack.size() - 2 );
	}

	public int depth() {
		return internalStack.size();
	}

	public boolean isEmpty() {
		return internalStack.isEmpty();
	}

	public void clear() {
		internalStack.clear();
	}
}
