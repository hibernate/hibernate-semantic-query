/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.hibernate.sqm.parser.ConsumerContext;
import org.hibernate.sqm.domain.BasicType;

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
			ConsumerContext consumerContext,
			boolean isDivision) {
		if ( isDivision ) {
			// covered under the note in 6.5.7.1 discussing the unportable
			// "semantics of the SQL division operation"..
			return consumerContext.getDomainMetamodel().getBasicType( Number.class );
		}
		else if ( Double.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( Double.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( Float.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( Float.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( BigDecimal.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( BigDecimal.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( BigInteger.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( BigInteger.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( Long.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( Long.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( Integer.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( Integer.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( Short.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return consumerContext.getDomainMetamodel().getBasicType( Integer.class );
		}
		else if ( Short.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return consumerContext.getDomainMetamodel().getBasicType( Integer.class );
		}
		else {
			return consumerContext.getDomainMetamodel().getBasicType( Number.class );
		}
	}

	public static BasicType resolveSingleNumericType(BasicType typeDescriptor, ConsumerContext consumerContext) {
		if ( Double.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( Float.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( BigDecimal.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( BigInteger.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( Long.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( Integer.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return typeDescriptor;
		}
		else if ( Short.class.isAssignableFrom( typeDescriptor.getJavaType() ) ) {
			return consumerContext.getDomainMetamodel().getBasicType( Integer.class );
		}
		else {
			return consumerContext.getDomainMetamodel().getBasicType( Number.class );
		}

	}
}
