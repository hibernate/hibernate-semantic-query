/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAttributePathResolverImpl implements AttributePathResolver {
	protected abstract FromElementBuilder fromElementBuilder();
	protected abstract ParsingContext parsingContext();

	protected FromElement resolveAnyIntermediateAttributePathJoins(
			FromElement lhs,
			String[] pathParts,
			int start) {
		int i = start;

		// build joins for any intermediate path parts
		while ( i < pathParts.length-1 ) {
			lhs = buildIntermediateAttributeJoin( lhs, pathParts[i] );
			i++;
		}

		return lhs;
	}

	protected FromElement buildIntermediateAttributeJoin(FromElement lhs, String pathPart) {
		final AttributeDescriptor joinedAttributeDescriptor = resolveAttributeDescriptor( lhs, pathPart );
		validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );
		return fromElementBuilder().buildAttributeJoin(
				lhs.getContainingSpace(),
				lhs,
				joinedAttributeDescriptor,
				null,
				getIntermediateJoinType(),
				areIntermediateJoinsFetched()
		);
	}

	protected void validateIntermediateAttributeJoin(FromElement lhs, AttributeDescriptor joinedAttributeDescriptor) {
	}

	protected JoinType getIntermediateJoinType() {
		return JoinType.LEFT;
	}

	protected boolean areIntermediateJoinsFetched() {
		return false;
	}

	protected AttributeDescriptor resolveAttributeDescriptor(FromElement lhs, String attributeName) {
		final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( attributeName );
		if ( attributeDescriptor == null ) {
			throw new SemanticException(
					"Name [" + attributeName + "] is not a valid attribute on from-element [" +
							lhs.getTypeDescriptor().getTypeName() + "]"
			);
		}

		return attributeDescriptor;
	}
}
