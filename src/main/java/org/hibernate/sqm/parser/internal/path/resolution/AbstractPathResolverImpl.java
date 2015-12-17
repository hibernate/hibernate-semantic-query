/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal.path.resolution;

import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.Bindable;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.parser.QueryException;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.parser.internal.FromElementBuilder;
import org.hibernate.sqm.parser.internal.ParsingContext;
import org.hibernate.sqm.path.AttributeBindingSource;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;

/**
 * Template support for PathResolver implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPathResolverImpl implements PathResolver {
	protected abstract FromElementBuilder fromElementBuilder();
	protected abstract ParsingContext parsingContext();

	protected AttributeBindingSource resolveAnyIntermediateAttributePathJoins(
			AttributeBindingSource lhs,
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

	protected AttributeBindingSource buildIntermediateAttributeJoin(AttributeBindingSource lhs, String pathPart) {
		final Attribute joinedAttributeDescriptor = resolveAttributeDescriptor( lhs, pathPart );
		validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );

		final QualifiedAttributeJoinFromElement join = buildAttributeJoin( lhs.getFromElement(), joinedAttributeDescriptor, null );

		return join;
	}

	protected QualifiedAttributeJoinFromElement buildAttributeJoin(
			FromElement lhsFromElement,
			Attribute joinedAttributeDescriptor,
			EntityType subclassIndicator) {
		return fromElementBuilder().buildAttributeJoin(
				lhsFromElement.getContainingSpace(),
				null,
				joinedAttributeDescriptor,
				subclassIndicator,
				lhsFromElement.asLoggableText() + '.' + joinedAttributeDescriptor.getName(),
				getIntermediateJoinType(),
				lhsFromElement,
				areIntermediateJoinsFetched()
		);
	}

	protected void validateIntermediateAttributeJoin(AttributeBindingSource lhs, Attribute joinedAttributeDescriptor) {
	}

	protected JoinType getIntermediateJoinType() {
		return JoinType.LEFT;
	}

	protected boolean areIntermediateJoinsFetched() {
		return false;
	}

	protected Attribute resolveAttributeDescriptor(AttributeBindingSource lhs, String attributeName) {
		final ManagedType managedType = resolveManagedType( lhs.getBoundModelType(), lhs.asLoggableText() );
		final Attribute attributeDescriptor = managedType.findAttribute( attributeName );
		if ( attributeDescriptor == null ) {
			throw new SemanticException(
					"Name [" + attributeName + "] is not a valid attribute from type [" +
							managedType + " (" + lhs.asLoggableText() + ")]"
			);
		}

		return attributeDescriptor;
	}

	protected ManagedType resolveManagedType(Bindable bindable, String path) {
		if ( bindable instanceof EntityType ) {
			return (ManagedType) bindable;
		}
		else if ( bindable instanceof SingularAttribute ) {
			final SingularAttribute singularAttribute = (SingularAttribute) bindable;
			if ( !ManagedType.class.isInstance( singularAttribute.getBoundType() ) ) {
				throw new SemanticException(
						"Expecting a ManagedType reference, but referenced SingularAttribute [" + path + "] type was not ManagedType : "
								+ singularAttribute.getBoundType()
				);
			}
			return (ManagedType) singularAttribute.getBoundType();
		}
		else if ( bindable instanceof PluralAttribute ) {
			final PluralAttribute pluralAttribute = (PluralAttribute) bindable;
			if ( !ManagedType.class.isInstance( pluralAttribute.getBoundType() ) ) {
				throw new SemanticException(
						"Expecting a ManagedType reference, but referenced SingularAttribute type was not ManagedType : "
								+ pluralAttribute.getBoundType()
				);
			}
			return (ManagedType) pluralAttribute.getBoundType();
		}
		else {
			throw new QueryException( "Could not interpret Bindable; unexpected type : " + bindable );
		}

	}

//	protected AttributeReferenceExpression makeAttributeReferenceExpression(FromElement lhs, String attributeName) {
//		final Attribute attribute = resolveAttributeDescriptor( lhs, attributeName );
//		( (Bindable) attribute ).getBoundType()
//		final Type type;
//		if ( attribute instanceof SingularAttribute ) {
//			type = ( (SingularAttribute) attribute ).getType();
//		}
//		else if ( attribute instanceof PluralAttribute ) {
//			type = ( (PluralAttribute) attribute ).getElementType();
//		}
//		else {
//			throw new ParsingException( "Resolved attribute was neither javax.persistence.metamodel.SingularAttribute nor javax.persistence.metamodel.PluralAttribute" );
//		}
//
//		return new AttributeReferenceExpression( lhs, attribute, type );
//	}
}
