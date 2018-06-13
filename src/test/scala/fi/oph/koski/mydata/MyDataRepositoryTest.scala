package fi.oph.koski.mydata

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.HttpStatus
import org.scalatest.{FreeSpec, Matchers}

class MyDataRepositoryTest extends FreeSpec with Matchers {

  val oid = "1.2.3.4.5" // student ID
  val memberId = "hsl"

  "MyDataRepository" - {
    "Kannan tyhjennys" in {
      KoskiApplicationForTests.mydataRepository.delete(oid, memberId)
    }
    "Käyttäjällä ei ole hyväksyntiä" in {
      KoskiApplicationForTests.mydataRepository.getAll(oid).toList.length should equal(0)
    }
    "Käyttäjä voi antaa hyväksynnän" in {
      KoskiApplicationForTests.mydataRepository.create(oid, memberId) should equal(true)
      KoskiApplicationForTests.mydataRepository.getAll(oid).toList.length should equal(1)
      KoskiApplicationForTests.mydataRepository.getAll(oid).toList.head.asiakas should equal(memberId)
    }
    "Käyttäjä voi poistaa hyväksynnän" in {
      KoskiApplicationForTests.mydataRepository.delete(oid, memberId) should equal(HttpStatus.ok)
      KoskiApplicationForTests.mydataRepository.getAll(oid).toList.length should equal(0)
    }
    "Käyttäjä voi päivittää hyväksynnän" in {
      val lastValidDate: LocalDate = LocalDate.now()
      KoskiApplicationForTests.mydataRepository.create(oid, memberId) should equal(true)
      KoskiApplicationForTests.mydataRepository.update(oid, memberId, lastValidDate) should equal(HttpStatus.ok)
      KoskiApplicationForTests.mydataRepository.getAll(oid).toList.head.voimassaAsti.toLocalDate should equal(lastValidDate)
    }
    "Vanhat hyväksynnät filtteröidään pois" in {
      KoskiApplicationForTests.mydataRepository.delete(oid, memberId) should equal(HttpStatus.ok)

      val lastValidDate: LocalDate = LocalDate.now().minusYears(2)
      KoskiApplicationForTests.mydataRepository.create(oid, memberId) should equal(true)
      KoskiApplicationForTests.mydataRepository.update(oid, memberId, lastValidDate) should equal(HttpStatus.ok)
      KoskiApplicationForTests.mydataRepository.getAllValid(oid).toList.length should equal(0)
    }
  }
}
