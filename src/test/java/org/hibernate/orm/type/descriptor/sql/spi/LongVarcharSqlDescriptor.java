/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.type.descriptor.sql.spi;

import java.sql.Types;

import org.hibernate.orm.type.descriptor.java.spi.BasicJavaTypeDescriptor;
import org.hibernate.orm.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#LONGVARCHAR LONGVARCHAR} handling.
 *
 * @author Steve Ebersole
 */
public class LongVarcharSqlDescriptor extends VarcharSqlDescriptor {
	public static final LongVarcharSqlDescriptor INSTANCE = new LongVarcharSqlDescriptor();

	public LongVarcharSqlDescriptor() {
	}

	@Override
	public BasicJavaTypeDescriptor getJdbcRecommendedJavaTypeMapping(TypeConfiguration typeConfiguration) {
		return (BasicJavaTypeDescriptor) super.getJdbcRecommendedJavaTypeMapping( typeConfiguration );
	}

	@Override
	public int getSqlType() {
		return Types.LONGVARCHAR;
	}
}
