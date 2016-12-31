/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.parser.criteria.tree.from;

import java.util.Collection;
import java.util.Map;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.JpaPredicate;
import org.hibernate.sqm.parser.criteria.tree.path.JpaPath;

/**
 * @author Steve Ebersole
 */
public interface JpaAttributeJoin<Z,X> extends Join<Z,X>, JpaFrom<Z,X> {

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Co-variant returns

	@Override
	JpaAttributeJoin<Z, X> on(Expression<Boolean> restriction);

	@Override
	JpaAttributeJoin<Z, X> on(Predicate... restrictions);

	@Override
	JpaPredicate getOn();

	@Override
	JpaFrom<?, Z> getParent();

	@Override
	<Y> JpaPath<Y> get(SingularAttribute<? super X, Y> attribute);

	@Override
	<E, C extends Collection<E>> JpaExpression<C> get(PluralAttribute<X, C, E> collection);

	@Override
	<K, V, M extends Map<K, V>> JpaExpression<M> get(MapAttribute<X, K, V> map);

	@Override
	<Y> JpaPath<Y> get(String attributeName);
}
