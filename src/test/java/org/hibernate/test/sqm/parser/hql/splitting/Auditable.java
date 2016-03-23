/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql.splitting;

import java.time.Instant;

/**
 * @author Steve Ebersole
 */
public interface Auditable {
	String getCreatedBy();
	Instant getCreatedAt();
	String getModifiedBy();
	Instant getModifiedAt();
}
