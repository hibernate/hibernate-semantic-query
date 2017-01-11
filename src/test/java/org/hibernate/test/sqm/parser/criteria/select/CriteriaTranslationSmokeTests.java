/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.criteria.select;

import javax.persistence.criteria.Root;

import org.hibernate.sqm.test.domain.Person;
import org.hibernate.sqm.test.domain.StandardModelTest;

import org.hibernate.test.sqm.parser.criteria.tree.CriteriaQueryImpl;
import org.junit.Test;

/**
 * @author Steve Ebersole
 */
public class CriteriaTranslationSmokeTests extends StandardModelTest {

	@Test
	public void smokeTest() {
		// Build a simple criteria ala `select e from Entity e`
		final CriteriaQueryImpl<Object> criteria = (CriteriaQueryImpl<Object>) criteriaBuilder.createQuery();
		Root root = criteria.from( Person.class );
		criteria.select( root );

		interpret( criteria );
	}
}
