/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;

/**
 * @author Steve Ebersole
 */
public class DmlRootAttributePathResolver extends StandardAttributePathResolverTemplate {
	private final RootEntityFromElement dmlRoot;
	private final FromElementBuilder fromElementBuilder;
	private final ParsingContext parsingContext;

	public DmlRootAttributePathResolver(
			RootEntityFromElement dmlRoot,
			FromElementBuilder fromElementBuilder,
			ParsingContext parsingContext) {
		this.dmlRoot = dmlRoot;
		this.fromElementBuilder = fromElementBuilder;
		this.parsingContext = parsingContext;
	}

	@Override
	protected FromElement findFromElementByAlias(String alias) {
		if ( alias.equals( dmlRoot.getAlias() ) ) {
			return dmlRoot;
		}
		else {
			return null;
		}
	}

	@Override
	protected FromElement findFromElementWithAttribute(String attributeName) {
		if ( dmlRoot.resolveAttribute( attributeName ) != null ) {
			return dmlRoot;
		}
		else {
			return null;
		}
	}

	@Override
	protected FromElementBuilder fromElementBuilder() {
		return fromElementBuilder;
	}

	@Override
	protected ParsingContext parsingContext() {
		return parsingContext;
	}
}
