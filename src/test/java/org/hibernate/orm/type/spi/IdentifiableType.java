/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.spi;

import org.hibernate.orm.type.descriptor.java.spi.IdentifiableJavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface IdentifiableType extends ManagedType {
	@Override
	IdentifiableType getSuperType();

	@Override
	IdentifiableJavaTypeDescriptor getJavaTypeDescriptor();
}
