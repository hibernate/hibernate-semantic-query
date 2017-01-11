/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sqm.test.domain;

import java.util.Map;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

/**
 * @author Steve Ebersole
 */
@Entity
public class EntityOfMaps {
	@Id
	public Integer id;

	@ElementCollection
	public Map<String,String> basicToBasicMap;

	@ElementCollection
	public Map<String,Component> basicToComponentMap;

	@ElementCollection
	public Map<Component,String> componentToBasicMap;

	@OneToMany
	public Map<String,EntityOfMaps> basicToOneToMany;

	@ManyToMany
	public Map<String,EntityOfMaps> basicToManyToMany;
}
