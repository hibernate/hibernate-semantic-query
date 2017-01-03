/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.test.sqm.type.spi;

/**
 * @author Steve Ebersole
 */
public interface SingularAttribute extends Attribute, javax.persistence.metamodel.SingularAttribute {
	enum Disposition {
		ID,
		VERSION,
		NORMAL
	}

	/**
	 * Returns whether this attribute is <ul>
	 *     <li>part of an id?</li>
	 *     <li>the version attribute?</li>
	 *     <li>or a normal attribute?</li>
	 * </ul>
	 */
	Disposition getDisposition();

	@Override
	default boolean isId() {
		return getDisposition() == Disposition.ID;
	}

	@Override
	default boolean isVersion() {
		return getDisposition() == Disposition.VERSION;
	}

	/**
	 * Obtain the attribute's type.
	 */
	Type getType();
}
