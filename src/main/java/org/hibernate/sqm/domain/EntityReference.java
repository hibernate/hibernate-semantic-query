/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.domain;

/**
 * Models a reference to an entity.
 *
 * @author Steve Ebersole
 */
public interface EntityReference extends SqmNavigableSource, PotentialEntityReferenceExporter {
	/**
	 * Obtain the name of the referenced entity
	 *
	 * @return The entity name
	 */
	String getEntityName();
}
