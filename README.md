<img src="http://static.jboss.org/hibernate/images/hibernate_logo_whitebkg_200px.png" />

## This project is no longer maintained separately.  It's code has been moved into Hibernate ORM proper.

It's original README is comtained below...

___

This project defines: 

* the Hibernate semantic query model (SQM), which is just a fancy way to say it defines
an object view of a query
* a walker (visitor) over SQM
* capability to interpret HQL/JPQL and JPA Criteria queries into SQM

The SQM can then be handed to Hibernate ORM (or other Hibernate projects accepting SQM)
and be executed.
