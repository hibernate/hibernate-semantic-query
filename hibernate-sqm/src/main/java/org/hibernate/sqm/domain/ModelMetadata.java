/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Information about the domain model space for the query.  This needs to come from the
 * consumer.
 *
 * @author Steve Ebersole
 */
public interface ModelMetadata {
	EntityTypeDescriptor resolveEntityReference(String reference);
}
