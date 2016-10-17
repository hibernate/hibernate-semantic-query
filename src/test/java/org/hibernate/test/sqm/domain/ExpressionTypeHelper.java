/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.domain;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;

/**
 * Helper for making the determination of an expression's "type" as sovered by the rules
 * defined in section 6.5.7.1 (Result Types of Expressions) of the JPA 2.1 spec
 *
 * @author Steve Ebersole
 */
public class ExpressionTypeHelper {
	private ExpressionTypeHelper() {
	}

	/**
	 * Determine the result type of an arithmetic operation as defined by the
	 * rules in section 6.5.7.1.
	 *
	 * @return The operation result type
	 */
	public static BasicType resolveArithmeticType(
			BasicType firstType,
			BasicType secondType,
			boolean isDivision,
			DomainMetamodel domainMetamodel) {
		if ( isDivision ) {
			// covered under the note in 6.5.7.1 discussing the unportable
			// "semantics of the SQL division operation"..
			return domainMetamodel.resolveBasicType( Number.class );
		}
		else if ( matchesJavaType( firstType, Double.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, Double.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, Float.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, Float.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, BigDecimal.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, BigDecimal.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, BigInteger.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, BigInteger.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, Long.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, Long.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, Integer.class ) ) {
			return firstType;
		}
		else if ( matchesJavaType( secondType, Integer.class ) ) {
			return secondType;
		}
		else if ( matchesJavaType( firstType, Short.class ) ) {
			return domainMetamodel.resolveBasicType( Integer.class );
		}
		else if ( matchesJavaType( secondType, Short.class ) ) {
			return domainMetamodel.resolveBasicType( Integer.class );
		}
		else {
			return domainMetamodel.resolveBasicType( Number.class );
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean matchesJavaType(BasicType type, Class javaType) {
		return type != null && javaType.isAssignableFrom( type.getJavaType() );
	}

	public static BasicType resolveSingleNumericType(
			BasicType typeDescriptor,
			DomainMetamodel domainMetamodel) {
		if ( matchesJavaType( typeDescriptor, Double.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, Float.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, BigDecimal.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, BigInteger.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, Long.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, Integer.class ) ) {
			return typeDescriptor;
		}
		else if ( matchesJavaType( typeDescriptor, Short.class ) ) {
			return domainMetamodel.resolveBasicType( Integer.class );
		}
		else {
			return domainMetamodel.resolveBasicType( Number.class );
		}
	}
}
