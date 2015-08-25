/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
