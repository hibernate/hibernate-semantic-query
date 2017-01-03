/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.domain;

import org.hibernate.sqm.query.expression.domain.NavigableBinding;
import org.hibernate.sqm.query.expression.domain.NavigableResolutionContext;
import org.hibernate.sqm.query.expression.domain.NavigableSourceBinding;

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
 */
public interface SqmNavigable extends DomainTypeExporter, Loggable {
	/**
	 * Returns the "left hand side" of this Navigable.  In the case of a
	 * "root" reference, this will return {@code null}.
	 */
	SqmNavigableSource getSource();

	// todo : because of how Navigables are "resolved" now into AttributeBindings here, we may no longer need all the specific exposed sub-interfaces
	//		on the ORM side, e.g., we'd still have specific impls for SqmNavigable (i.e. the org.hibernate.persister.common.spi.Attribute stuff)
	//		to be able to split handling the concrete types (typical visitor pattern), but SQM can just call this generic
	//		#makeBinding method
	NavigableBinding makeBinding(NavigableSourceBinding sourceBinding, NavigableResolutionContext resolutionContext);
}
