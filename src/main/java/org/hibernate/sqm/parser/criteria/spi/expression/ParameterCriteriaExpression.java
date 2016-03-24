/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.parser.criteria.spi.expression;

import javax.persistence.criteria.ParameterExpression;

/**
 * @author Steve Ebersole
 */
public interface ParameterCriteriaExpression<T> extends CriteriaExpression<T>, ParameterExpression<T> {
	String getName();
	Integer getPosition();
}
