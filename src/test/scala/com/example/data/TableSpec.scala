package com.example.data

import org.h2.jdbc.JdbcSQLException
import org.scalatest.Succeeded

import scala.concurrent.Await
import scala.concurrent.duration._

class TableSpec extends SpecBase {

  import dc.driver.api._
  val db = dc.db

  case class Dragon(id: Option[Int], name: String)

  class DragonTable(tag: Tag) extends Table[Dragon](tag, "DRAGONS") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name", O.Length(2))

//    def firepower = column[Int]("firepower")

    def * = (id.?, name) <>(upcased, downcased)
//    def * = (id.?, name) <>(Dragon.tupled, Dragon.unapply)
//    def * = (id.?, name, firepower) <>(Dragon.tupled, Dragon.unapply)
  }

  def upcased(tuple: (Option[Int], String)): Dragon = Dragon(tuple._1, tuple._2.toUpperCase)
  def downcased(dragon: Dragon): Option[(Option[Int], String)] = Some((dragon.id, dragon.name.toLowerCase))

  val dragonTable = TableQuery[DragonTable]
  val createTable = dragonTable.schema.create

  describe("tables") {

    it("size restriction"){
      Await.result(db.run(createTable), 10 seconds)
      val createSabretooth = dragonTable += Dragon(None, "Sabretooth")
      recoverToSucceededIf[JdbcSQLException] { db.run(createSabretooth) map {_ should be(Some(1))} }
    }

    it("adding firepower") {
      val createToothless = dragonTable += Dragon(None, "Bo") //, 25)
      db.run(createToothless andThen dragonTable.result) map { dragons =>
        println(dragons)
        Succeeded
      }
    }
  }
}
