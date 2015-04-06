package database.columns

import scala.slick.driver.MySQLDriver.simple._
/**
 * Created by xmax on 7/4/15.
 */

trait IdColumn[I] {
  def id: Column[I]
}

trait VersionColumn {
  def version: Column[Long]
}