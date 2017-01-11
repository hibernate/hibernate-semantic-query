/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

/**
 * @author Steve Ebersole
 */
@Entity
public class EntityOfLists {
	@Id
	public Integer id;

	@ElementCollection
	@OrderColumn
	public List<String> listOfBasics;

	@ElementCollection
	@OrderColumn
	public List<Component> listOfComponents;

	@OneToMany
	@OrderColumn
	public List<EntityOfLists> listOfOneToMany;

	@ManyToMany
	@OrderColumn
	public List<EntityOfLists> listOfManyToMany;

}
