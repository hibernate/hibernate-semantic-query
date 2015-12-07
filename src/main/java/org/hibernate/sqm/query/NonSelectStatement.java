/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

/**
 * Used to more easily identifier non-SELECT (DML) statements by gross type.
 *
 * @author Steve Ebersole
 */
public interface NonSelectStatement extends Statement {
}
