/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.common;

import org.hibernate.sqm.query.Parameter;

/**
 * Describes the context in which a parameter is declared.  This is  used mainly to
 * determine metadata about the parameter ({@link Parameter#allowMultiValuedBinding()}, e.g.)
 *
 * @author Steve Ebersole
 */
public interface ParameterDeclarationContext {
	/**
	 * Are multi-valued parameter bindings allowed in this context?
	 *
	 * @return {@code true} if they are; {@code false} otherwise.
	 */
	boolean isMultiValuedBindingAllowed();
}
