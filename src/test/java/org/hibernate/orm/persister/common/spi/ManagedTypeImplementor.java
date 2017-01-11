/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.persister.common.spi;

import java.util.Map;
import java.util.function.Consumer;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

/**
 * @author Steve Ebersole
 */
public interface ManagedTypeImplementor<T> extends ManagedType<T>, CompositeContainer<T>, ExpressableType<T> {
	ManagedTypeImplementor<? super T> getSuperType();

	@Override
	OrmNavigableSource getSource();

	OrmAttribute findDeclaredAttribute(String name);
	OrmAttribute findDeclaredAttribute(String name, Class resultType);

	OrmAttribute findAttribute(String name);

	Map<String, OrmAttribute> getDeclaredAttributesByName();

	<A extends Attribute> void collectDeclaredAttributes(Consumer<A> collector, Class<A> restrictionType);

	<A extends Attribute> void collectAttributes(Consumer<A> collector, Class<A> restrictionType);

	Map<String, OrmAttribute> getAttributesByName();
}
