/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.domain.EntityTypeDescriptor;

/**
 * Information about the domain model space for the query.  This needs to come from the
 * consumer.
 *
 * @author Steve Ebersole
 */
public interface ModelMetadata {
	EntityTypeDescriptor resolveEntityReference(String reference);
}
