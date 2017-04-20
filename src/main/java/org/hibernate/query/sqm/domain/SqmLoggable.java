/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

/**
 * @author Steve Ebersole
 *
 * @deprecated Added a {@link org.hibernate.persister.common.spi.Navigable#asLoggableText}
 * replacement already.
 */
@Deprecated
public interface SqmLoggable {
	/**
	 * Obtain a loggable representation.
	 *
	 * @return The loggable representation of this reference
	 */
	String asLoggableText();
}
