/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query;

/**
 * @author Steve Ebersole
 */
public interface SqmSelectStatement extends SqmStatement {
	SqmQuerySpec getQuerySpec();
}
