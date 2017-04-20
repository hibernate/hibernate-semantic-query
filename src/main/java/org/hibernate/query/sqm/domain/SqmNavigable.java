/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.domain;

/**
 * Represents something that can be incorporated into a query "path".  JPA calls this
 * {@link javax.persistence.metamodel.Bindable}, but that name is misleading IMO so we
 * call it Navigable to convey the idea that these represent navigation (dot references)
 * across an "attribute path" ({@code a.addressType} or {@code a.city.state} e.g.)
 * <p/>
 * Navigable represents a single piece of these paths.
 * <p/>
 * A Navigable may (e.g., a ManyToOne) or may not (e.g., Basic) itself also be a NavigableSource
 *
 * @author Steve Ebersole
 *
 * @deprecated replace with {@link org.hibernate.persister.common.spi.Navigable} and
 * {@link org.hibernate.persister.common.spi.NavigableSource}
 */
@Deprecated
public interface SqmNavigable<J> extends SqmDomainTypeExporter<J>, SqmLoggable, SqmExpressableType<J> {
	/**
	 * Returns the "left hand side" of this Navigable.  In the case of a
	 * "root" reference, this will return {@code null}.
	 *
	 * todo (6.0) - on Navigable, this would (and does already via override) return NavigableSource
	 */
	SqmNavigableSource getSource();

	/**
	 * todo (6.0) - replace with NavigableRole on Navigable?
	 * 		- the name is simply the NavigableRole's "local name", so seems redundant here
	 */
	String getNavigableName();
}
