/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Steve Ebersole
 */
@Entity
public class Person {
	@Embeddable
	public static class Name {
		public String first;
		public String last;
	}

	@Id
	public Integer pk;

	@Embedded
	public Name name;

	public String nickName;

	@ManyToOne
	Person mate;

	@Temporal( TemporalType.TIMESTAMP )
//	public Instant dob;
	public Date dob;

	public int numberOfToes;
}
