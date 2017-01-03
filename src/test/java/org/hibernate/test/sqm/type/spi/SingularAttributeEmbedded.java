/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

/**
 * A singular attribute whose value (DomainType) is an embedded/embeddable type
 *
 * @author Steve Ebersole
 */
public interface SingularAttributeEmbedded extends SingularAttribute {
	@Override
	EmbeddableType getType();
}
