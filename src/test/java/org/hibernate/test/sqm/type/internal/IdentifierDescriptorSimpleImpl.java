/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.internal;

import java.lang.reflect.Member;

import org.hibernate.test.sqm.type.spi.BasicType;
import org.hibernate.test.sqm.type.spi.IdentifiableType;
import org.hibernate.test.sqm.type.spi.IdentifierDescriptorSimple;
import org.hibernate.test.sqm.type.spi.SingularAttribute;
import org.hibernate.test.sqm.type.spi.SingularAttributeBasic;
import org.hibernate.test.sqm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public class IdentifierDescriptorSimpleImpl implements IdentifierDescriptorSimple {
	private final SingularAttributeBasic idAttribute;

	public IdentifierDescriptorSimpleImpl(
			IdentifiableType entityType,
			String idAttributeName,
			BasicType idType) {
		this.idAttribute = new SingularAttributeBasicImpl(
				entityType,
				idAttributeName,
				idType,
				SingularAttribute.Disposition.ID,
				resolveAttributeMember( entityType, idAttributeName, idType ),
				false
		);
	}

	private static Member resolveAttributeMember(IdentifiableType entityType, String idAttributeName, Type idType) {
		if ( entityType == null || entityType.getJavaType() == null ) {
			return null;
		}

		return JavaTypeHelper.resolveAttributeMember( entityType.getJavaType(), idAttributeName, idType );
	}

	@Override
	public BasicType getIdType() {
		return idAttribute.getType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public SingularAttributeBasic getIdAttribute() {
		return idAttribute;
	}

	@Override
	public String getReferableAttributeName() {
		return idAttribute.getName();
	}
}
