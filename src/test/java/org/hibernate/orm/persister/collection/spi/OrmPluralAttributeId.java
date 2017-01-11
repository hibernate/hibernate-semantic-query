/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.orm.persister.collection.spi;

import org.hibernate.id.IdentifierGenerator;
import org.hibernate.orm.persister.common.spi.OrmTypeExporter;
import org.hibernate.orm.type.spi.BasicType;
import org.hibernate.orm.type.spi.Type;

/**
 * @author Steve Ebersole
 */
public class OrmPluralAttributeId implements OrmTypeExporter {
	private final BasicType type;
	private final IdentifierGenerator generator;

	public OrmPluralAttributeId(BasicType type, IdentifierGenerator generator) {
		this.type = type;
		this.generator = generator;
	}

	@Override
	public Type getOrmType() {
		return type;
	}

	public IdentifierGenerator getGenerator() {
		return generator;
	}
}
