/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.path;

import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Represents a specific instance/usage of a Bindable.  E.g. consider a query
 * like {@code ... from Person p, Person p2 ... }.  Here we have the one
 * Bindable ({@code Person}) but 2 Bindings (identified by {@code p}
 * and {@code p2}).
 * <p/>
 * The JPA corollary to Binding would be {@link javax.persistence.criteria.Path}.
 *
 * @author Steve Ebersole
 */
public interface Binding extends SqmExpression {
	/**
	 * Obtain the Bindable referenced by this Binding.
	 *
	 * @return The bound Bindable
	 */
	Bindable getBindable();

	/**
	 * For cases where a downcast (TREAT) has been applied, what
	 * was the specific subclass downcast to?
	 *
	 * @return The specific subclass.
	 */
	ManagedType getSubclassIndicator();

	SqmFrom getFromElement();

	/**
	 * Obtain a loggable representation of this path expression, ideally back to
	 * its source form.
	 *
	 * @return
	 */
	String asLoggableText();
}
