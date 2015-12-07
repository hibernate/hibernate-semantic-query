/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import org.hibernate.sqm.domain.IdentifiableType;
import org.hibernate.sqm.domain.MappedSuperclassType;

/**
 * @author Steve Ebersole
 */
public class MappedSuperclassTypeImpl extends AbstractIdentifiableType implements MappedSuperclassType {
	public MappedSuperclassTypeImpl(Class javaType, IdentifiableType superType) {
		super( javaType, superType );
	}
}
