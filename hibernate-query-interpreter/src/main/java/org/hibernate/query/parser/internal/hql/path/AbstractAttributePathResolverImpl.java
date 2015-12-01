/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.query.parser.internal.hql.path;

import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
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
		final Attribute joinedAttributeDescriptor = resolveAttributeDescriptor( lhs, pathPart );
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

	protected void validateIntermediateAttributeJoin(FromElement lhs, Attribute joinedAttributeDescriptor) {
	}

	protected JoinType getIntermediateJoinType() {
		return JoinType.LEFT;
	}

	protected boolean areIntermediateJoinsFetched() {
		return false;
	}

	protected Attribute resolveAttributeDescriptor(FromElement lhs, String attributeName) {
		final Attribute attributeDescriptor = lhs.resolveAttribute( attributeName );
		if ( attributeDescriptor == null ) {
			throw new SemanticException(
					"Name [" + attributeName + "] is not a valid attribute on from-element [" +
							lhs.getBindableModelDescriptor() + "(" + lhs.getAlias() + ")]"
			);
		}

		return attributeDescriptor;
	}

	protected AttributeReferenceExpression makeAttributeReferenceExpression(FromElement lhs, String attributeName) {
		final Attribute attribute = resolveAttributeDescriptor( lhs, attributeName );
		final Type type;
		if ( attribute instanceof SingularAttribute ) {
			type = ( (SingularAttribute) attribute ).getType();
		}
		else if ( attribute instanceof PluralAttribute ) {
			type = ( (PluralAttribute) attribute ).getCollectionElementType();
		}
		else {
			throw new ParsingException( "Resolved attribute was neither javax.persistence.metamodel.SingularAttribute nor javax.persistence.metamodel.PluralAttribute" );
		}

		return new AttributeReferenceExpression( lhs, attribute, type );
	}
}
