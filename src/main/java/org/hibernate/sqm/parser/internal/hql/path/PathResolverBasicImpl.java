/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.hql.path;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PathResolverBasicImpl extends PathResolverStandardTemplate {
	private static final Logger log = Logger.getLogger( PathResolverBasicImpl.class );

	public PathResolverBasicImpl(ResolutionContext resolutionContext) {
		super( resolutionContext );
	}
}
