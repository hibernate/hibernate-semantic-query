/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.test.sqm.parser.hql.splitting;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Ebersole
 */
@Entity
public class Fund implements Auditable {
	@Id
	private Integer id;
	private String createdBy;
	private Instant createdAt;
	private String modifiedBy;
	private Instant modifiedAt;

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public Instant getCreatedAt() {
		return createdAt;
	}

	@Override
	public String getModifiedBy() {
		return modifiedBy;
	}

	@Override
	public Instant getModifiedAt() {
		return modifiedAt;
	}
}
