/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sqm.query.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.Parameter;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmStatement implements SqmStatement, ParameterCollector {
	private Map<String,NamedParameterSqmExpression> namedQueryParameters;
	private Map<Integer,PositionalParameterSqmExpression> positionalQueryParameters;

	@Override
	public void addParameter(NamedParameterSqmExpression parameter) {
		assert parameter.getName() != null;
		assert parameter.getPosition() == null;

		if ( namedQueryParameters == null ) {
			namedQueryParameters = new ConcurrentHashMap<>();
		}

		namedQueryParameters.put( parameter.getName(), parameter );
	}

	@Override
	public void addParameter(PositionalParameterSqmExpression parameter) {
		assert parameter.getPosition() != null;
		assert parameter.getName() == null;

		if ( positionalQueryParameters == null ) {
			positionalQueryParameters = new ConcurrentHashMap<>();
		}

		positionalQueryParameters.put( parameter.getPosition(), parameter );
	}

	public void wrapUp() {
		validateParameters();
	}

	private void validateParameters() {
		if ( positionalQueryParameters == null ) {
			return;
		}

		// validate the positions.  JPA says that these should start with 1 and
		// increment contiguously (no gaps)
		int[] positionsArray = positionalQueryParameters.keySet().stream().mapToInt( Integer::intValue ).toArray();
		Arrays.sort( positionsArray );

		int previous = 0;
		for ( Integer position : positionsArray ) {
			if ( position != previous + 1 ) {
				if ( previous == 0 ) {
					throw new SemanticException( "Positional parameters did not start with 1 : " + position );
				}
				else {
					throw new SemanticException( "Gap in positional parameter positions; skipped " + (previous+1) );
				}
			}
			previous = position;
		}
	}

	@Override
	public Set<Parameter> getQueryParameters() {
		Set<Parameter> parameters = new HashSet<>();
		if ( namedQueryParameters != null ) {
			parameters.addAll( namedQueryParameters.values() );
		}
		if ( positionalQueryParameters != null ) {
			parameters.addAll( positionalQueryParameters.values() );
		}
		return parameters;
	}
}
