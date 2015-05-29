package schema

import database.tableQueries.TableWithIdQuery

import scala.util.Try

/**
 * Created by pnagarjuna on 21/05/15.
 */
object EntityUtils {
  import scala.slick.driver.MySQLDriver.simple._

  lazy val entities = new TableWithIdQuery[Entity, Long, Entities](tag => new Entities(tag)) {
    /**
     * Extracts the model Id of a arbitrary model.
     * @param model a mapped model
     * @return a Some[I] if Id is filled, None otherwise
     */
    override def extractId(model: Entity): Option[Long] = model.id

    /**
     *
     * @param model a mapped model (usually without an assigned id).
     * @param id an id, usually generate by the database
     * @return a model M with an assigned id.
     */
    override def withId(model: Entity, id: Long): Entity = model.copy(id = Some(id))
  }

  def getEntity(id: Long)(implicit db: Database): Try[Entity] = db.withSession {implicit sx => {
    entities.tryFindById(id)
  }}


}
