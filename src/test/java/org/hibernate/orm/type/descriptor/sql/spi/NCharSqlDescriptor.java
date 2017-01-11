/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.Types;

/**
 * Descriptor for {@link Types#NCHAR NCHAR} handling.
 *
 * @author Steve Ebersole
 */
public class NCharSqlDescriptor extends NVarcharSqlDescriptor {
	public static final NCharSqlDescriptor INSTANCE = new NCharSqlDescriptor();

	public NCharSqlDescriptor() {
	}

	@Override
	public int getSqlType() {
		return Types.NCHAR;
	}
}
