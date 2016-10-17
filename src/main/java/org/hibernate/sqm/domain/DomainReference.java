/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Generalized contract for references to any part of an application's domain
 * model in the query.  These references are resolved through the
 * {@link org.hibernate.sqm.ConsumerContext}.
 * <p/>
 * The design principle here is that of a memento (pattern).  This is designed
 * to allow the consumer to hand us "contextual" representation of these
 * references which we will incorporate in the SQM tree.  Later, as the consumer
 * processes the tree and encounters these references they can handle them
 * directly (as they built them in the first place).
 *
 * @author Steve Ebersole
 */
public interface DomainReference {
	/**
	 * Obtain a loggable representation of this path expression, ideally back to
	 * its source form.
	 *
	 * @return The loggable representation of this reference
	 */
	String asLoggableText();
}
