/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.sqm.produce.paths.spi;

import org.hibernate.query.sqm.produce.navigable.spi.NavigableReferenceBuilderContext;
import org.hibernate.sqm.parser.common.ParsingContext;
import org.hibernate.sqm.parser.common.QuerySpecProcessingState;

/**
 * @author Steve Ebersole
 */
public interface SemanticPathResolutionContext {
	ParsingContext getParsingContext();

	QuerySpecProcessingState getQuerySpecProcessingState();

	NavigableReferenceBuilderContext getNavigableReferenceBuilderContext();
}
