/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;

/**
 * @author Steve Ebersole
 */
public interface ParameterCollector {
	void addParameter(NamedParameterSqmExpression parameter);
	void addParameter(PositionalParameterSqmExpression parameter);
}
