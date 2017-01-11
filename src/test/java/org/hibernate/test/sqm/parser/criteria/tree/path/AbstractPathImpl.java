/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.tree.path;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.sqm.NotYetImplementedException;
import org.hibernate.sqm.domain.SqmNavigable;
import org.hibernate.sqm.parser.criteria.tree.JpaExpression;
import org.hibernate.sqm.parser.criteria.tree.path.JpaPath;
import org.hibernate.sqm.parser.criteria.tree.path.JpaPathSource;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaBuilderImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.AbstractJpaExpressionImpl;
import org.hibernate.test.sqm.parser.criteria.tree.expression.PathTypeExpression;

/**
 * Convenience base class for various {@link Path} implementations.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPathImpl<X>
		extends AbstractJpaExpressionImpl<X>
		implements JpaPath<X>, JpaPathSource<X>, Serializable {

	private final JpaPathSource pathSource;
	private final JpaExpression<Class<? extends X>> typeExpression;
	private Map<String,Path> attributePathRegistry;

	/**
	 * Constructs a basic path instance.
	 *
	 * @param javaType The java type of this path
	 * @param criteriaBuilder The criteria builder
	 * @param pathSource The source (or origin) from which this path originates
	 */
	@SuppressWarnings({ "unchecked" })
	public AbstractPathImpl(
			CriteriaBuilderImpl criteriaBuilder,
			SqmNavigable sqmNavigable,
			Class<X> javaType,
			JpaPathSource pathSource) {
		super( criteriaBuilder, sqmNavigable, javaType );
		this.pathSource = pathSource;
		this.typeExpression =  new PathTypeExpression( criteriaBuilder(), sqmNavigable, getJavaType(), this );
	}

	public JpaPathSource getPathSource() {
		return pathSource;
	}

	@Override
	public JpaPathSource<?> getParentPath() {
		return getPathSource();
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public JpaExpression<Class<? extends X>> type() {
		return typeExpression;
	}


	public abstract String getPathIdentifier();

	protected abstract boolean canBeDereferenced();

	protected final RuntimeException illegalDereference() {
		return new IllegalStateException(
				String.format(
						"Illegal attempt to dereference path source [%s] of basic type",
						getPathIdentifier()
				)
		);
//		String message = "Illegal attempt to dereference path source [";
//		if ( source != null ) {
//			message += " [" + getPathIdentifier() + "]";
//		}
//		return new IllegalArgumentException(message);
	}

	protected final RuntimeException unknownAttribute(String attributeName) {
		String message = "Unable to resolve attribute [" + attributeName + "] against path";
		JpaPathSource<?> source = getPathSource();
		if ( source != null ) {
			message += " [" + source.getPathIdentifier() + "]";
		}
		return new IllegalArgumentException(message);
	}

	protected final Path resolveCachedAttributePath(String attributeName) {
		return attributePathRegistry == null
				? null
				: attributePathRegistry.get( attributeName );
	}

	protected final void registerAttributePath(String attributeName, Path path) {
		if ( attributePathRegistry == null ) {
			attributePathRegistry = new HashMap<String,Path>();
		}
		attributePathRegistry.put( attributeName, path );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <Y> JpaPath<Y> get(SingularAttribute<? super X, Y> attribute) {
//		if ( ! canBeDereferenced() ) {
//			throw illegalDereference();
//		}
//
//		SingularAttributePath<Y> path = (SingularAttributePath<Y>) resolveCachedAttributePath( attribute.getName() );
//		if ( path == null ) {
//			path = new SingularAttributePath<Y>(
//					criteriaBuilder(),
//					attribute.getJavaType(),
//					getPathSourceForSubPaths(),
//					attribute
//			);
//			registerAttributePath( attribute.getName(), path );
//		}
//		return path;

		throw new NotYetImplementedException();
	}

	protected JpaPathSource getPathSourceForSubPaths() {
		return this;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <E, C extends Collection<E>> JpaExpression<C> get(PluralAttribute<X, C, E> attribute) {
//		if ( ! canBeDereferenced() ) {
//			throw illegalDereference();
//		}
//
//		PluralAttributePath<C> path = (PluralAttributePath<C>) resolveCachedAttributePath( attribute.getName() );
//		if ( path == null ) {
//			path = new PluralAttributePath<C>( criteriaBuilder(), this, attribute );
//			registerAttributePath( attribute.getName(), path );
//		}
//		return path;

		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <K, V, M extends Map<K, V>> JpaExpression<M> get(MapAttribute<X, K, V> attribute) {
//		if ( ! canBeDereferenced() ) {
//			throw illegalDereference();
//		}
//
//		PluralAttributePath path = (PluralAttributePath) resolveCachedAttributePath( attribute.getName() );
//		if ( path == null ) {
//			path = new PluralAttributePath( criteriaBuilder(), this, attribute );
//			registerAttributePath( attribute.getName(), path );
//		}
//		return path;

		throw new NotYetImplementedException(  );
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <Y> JpaPath<Y> get(String attributeName) {
//		if ( ! canBeDereferenced() ) {
//			throw illegalDereference();
//		}
//
//		final Attribute attribute = locateAttribute( attributeName );
//
//		if ( attribute.isCollection() ) {
//			final PluralAttribute<X,Y,?> pluralAttribute = (PluralAttribute<X,Y,?>) attribute;
//			if ( PluralAttribute.CollectionType.MAP.equals( pluralAttribute.getCollectionType() ) ) {
//				return (PluralAttributePath<Y>) this.<Object,Object,Map<Object, Object>>get( (MapAttribute) pluralAttribute );
//			}
//			else {
//				return (PluralAttributePath<Y>) this.get( (PluralAttribute) pluralAttribute );
//			}
//		}
//		else {
//			return get( (SingularAttribute<X,Y>) attribute );
//		}

		throw new NotYetImplementedException(  );
	}

	/**
	 * Get the attribute by name from the underlying model.  This allows subclasses to
	 * define exactly how the attribute is derived.
	 *
	 * @param attributeName The name of the attribute to locate
	 *
	 * @return The attribute; should never return null.
	 *
	 * @throws IllegalArgumentException If no such attribute exists
	 */
	protected  final Attribute locateAttribute(String attributeName) {
		final Attribute attribute = locateAttributeInternal( attributeName );
		if ( attribute == null ) {
			throw unknownAttribute( attributeName );
		}
		return attribute;
	}

	/**
	 * Get the attribute by name from the underlying model.  This allows subclasses to
	 * define exactly how the attribute is derived.  Called from {@link #locateAttribute}
	 * which also applies nullness checking for proper error reporting.
	 *
	 * @param attributeName The name of the attribute to locate
	 *
	 * @return The attribute; may be null.
	 *
	 * @throws IllegalArgumentException If no such attribute exists
	 */
	protected abstract Attribute locateAttributeInternal(String attributeName);

}
