package fi.oph.koski.etk

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.{File, PrintWriter}
import java.sql.Timestamp
import java.time.LocalDate.{of => date}
import scala.io.Source

class ElaketurvakeskusSpec
  extends AnyFreeSpec
    with DirtiesFixtures
    with KoskiHttpSpec
    with Matchers
    with RaportointikantaTestMethods
    with OpiskeluoikeusTestMethodsAmmatillinen {

  override protected def alterFixture(): Unit = {
    insertAdditionalTestData
    reloadRaportointikanta
  }

  "Elaketurvakeskus API" - {

    "Voidaan lähettää pelkkä tiedosto" in {
      val file = createFile()
      postCsvFile(file) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EtkResponse](body).tutkintojenLkm should be > 5
      }
    }
    "Voidaan jättää tiedosto lähettämättä" in {
      postRequest(TutkintotietoRequest(date(2016, 1, 1), date(2016, 12, 31), 2016)) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EtkResponse](body).tutkintojenLkm should be > 0
      }
    }
    "Tutkintotietojen muodostaminen" - {
      lazy val aineisto = {
        val file = createFile()
        val request = TutkintotietoRequest(date(2016, 1, 1), date(2016, 12, 31), 2016)
        val response = postFullRequest(request, file) {
          JsonSerializer.parse[EtkResponse](body)
        }
        file.delete
        response
      }

      "Aikaleima" in {
        aineisto.aikaleima shouldBe a[Timestamp]
      }
      "Lukumäärä" in {
        aineisto.tutkintojenLkm should be > 10
      }
      "Vuosi" in {
        aineisto.vuosi shouldBe (2016)
      }
      "Formatoi tutkintojen tasot oikein" in {
        aineisto.tutkinnot.flatMap(_.tutkinto.tutkinnonTaso).toSet should equal(Set(
          "ammatillinenperustutkinto",
          "ammattikorkeakoulututkinto",
          "alempikorkeakoulututkinto",
          "ylempiammattikorkeakoulututkinto",
          "ylempikorkeakoulututkinto"
        ))
      }
      "Parsii csv:n datat" in {
        aineisto.tutkinnot.filter(_.henkilö.hetu.contains("021094-650K")).toSet should equal(Set(
          EtkTutkintotieto(
            EtkHenkilö(Some("021094-650K"), Some(date(1989, 2, 1)), "Nenäkä", "Dtes Apu"),
            EtkTutkinto(Some("alempikorkeakoulututkinto"), Some(date(2011, 8, 1)), Some(date(2016, 6, 19))),
            None
          )
        ))
        aineisto.tutkinnot.filter(_.oid.contains("1.2.246.562.24.86863218011")).toSet should equal(Set(
          EtkTutkintotieto(
            EtkHenkilö(None, Some(date(1988, 2, 2)), "Kai", "Betat Testitap"),
            EtkTutkinto(Some("ylempiammattikorkeakoulututkinto"), Some(date(2015, 8, 1)), Some(date(2017, 6, 6))),
            Some(EtkViite(None, None, Some("1.2.246.562.24.86863218011")))
          )
        ))
      }
      "Hakee raportointkannasta datat" in {
        val opiskeluoikeudenOid = lastOpiskeluoikeus(ammattilainen.oid).oid

        aineisto.tutkinnot.filter(_.henkilö.hetu.contains(ammattilainen.hetu.get)).toSet should equal(Set(
          EtkTutkintotieto(
            EtkHenkilö(ammattilainen.hetu, Some(date(1918, 6, 28)), ammattilainen.sukunimi, ammattilainen.etunimet),
            EtkTutkinto(Some("ammatillinenperustutkinto"), Some(date(2012, 9, 1)), Some(date(2016, 5, 31))),
            Some(EtkViite(opiskeluoikeudenOid, Some(1), Some(ammattilainen.oid)))
          )
        ))
      }
      "Hakee raportointkannasta datat jättäen alle 18 vuotiaana valmistuneet pois" in {
        val opiskeluoikeudenOid = lastOpiskeluoikeus(etk18vSyntynytKesäkuunEnsimmäisenäPäivänä.oid).oid

        aineisto.tutkinnot.filter(_.henkilö.hetu.contains(etk18vSyntynytKesäkuunEnsimmäisenäPäivänä.hetu.get)).toSet should equal(Set())
      }
      "Hakee raportointkannasta datat ottaen 18 vuotissyntymäpäivänä valmistuneen mukaan" in {
        val opiskeluoikeudenOid = lastOpiskeluoikeus(etk18vSyntynytToukokuunViimeisenäPäivänä.oid).oid

        aineisto.tutkinnot.filter(_.henkilö.hetu.contains(etk18vSyntynytToukokuunViimeisenäPäivänä.hetu.get)).toSet should equal(Set(
          EtkTutkintotieto(
            EtkHenkilö(etk18vSyntynytToukokuunViimeisenäPäivänä.hetu, Some(date(1998, 5, 31)),
              etk18vSyntynytToukokuunViimeisenäPäivänä.sukunimi, etk18vSyntynytToukokuunViimeisenäPäivänä.etunimet),
            EtkTutkinto(Some("ammatillinenperustutkinto"), Some(date(2012, 9, 1)), Some(date(2016, 5, 31))),
            Some(EtkViite(opiskeluoikeudenOid, Some(1), Some(etk18vSyntynytToukokuunViimeisenäPäivänä.oid)))
          )
        ))
      }
      "Slave-oidin opinnot löytyvät masterille" in {
        val opiskeluoikeudenOid = getOpiskeluoikeudet(slave.henkilö.oid).find(_.tyyppi.koodiarvo == "ammatillinenkoulutus").flatMap(_.oid)

        aineisto.tutkinnot.filter(_.henkilö.hetu.contains(master.hetu.get)).toSet should equal(Set(
          EtkTutkintotieto(
            EtkHenkilö(master.hetu, Some(date(1997, 10, 10)), master.sukunimi, master.etunimet),
            EtkTutkinto(Some("ammatillinenperustutkinto"), Some(date(2012, 9, 1)), Some(date(2016, 5, 31))),
            Some(EtkViite(opiskeluoikeudenOid, Some(1), Some(slave.henkilö.oid)))
          )
        ))

      }
    }
    "Auditlogit" - {
      "Luo auditlogin, kun oid on ok" in {
        AuditLogTester.clearMessages
        val file = createFile(createCsv(createLine(oid = ammattilainen.oid)))
        postCsvFile(file) {
          verifyAuditLogMessage(ammattilainen)
        }
        file.delete
      }
      "Ei luoda auditlogia, jos ei hetua ja virheellinen oid" in {
        AuditLogTester.clearMessages
        val file = createFile(createCsv(createLine(oid = "1337")))
        postCsvFile(file) {
          AuditLogTester.getLogMessages should equal(Nil)
        }
        file.delete
      }
      "Luodaan auditlog, jos oid puuttuu mutta hetu löytyy" in {
        AuditLogTester.clearMessages
        val file = createFile(createCsv(createLine(hetu = eero.hetu.get, oid = "1337")))
        postCsvFile(file) {
          verifyAuditLogMessage(eero)
        }
        file.delete
      }
    }
    "Csv" - {
      "Jos otsikoita puuttuu" in {
        val headline = defaultHeadline.replace("hetu;", "")
        val file = createFile(createCsv(createLine(), headline = headline))
        an[Error] should be thrownBy (VirtaCsvParser.parse(Source.fromFile(file)))
        file.delete
      }
      "Jos riviltä puuttuu kenttiä" in {
        val rivi = createLine().replace("Nenäkä;", "")
        val file = createFile(createCsv(rivi))
        an[Error] should be thrownBy (VirtaCsvParser.parse(Source.fromFile(file)))
        file.delete
      }
    }
  }

  val defaultHeadline = "vuosi;korkeakoulu;hetu;syntymaaika;sukupuoli;oppijanumero;sukunimi;etunimet;tutkintokoodi;suorituspaivamaara;tutkinnon_taso;aloituspaivamaara;OpiskeluoikeudenAlkamispaivamaara"
  val csvFilePath = "csv-tiedosto-testia-varten.csv"

  val mockCsv =
    """|vuosi;korkeakoulu;hetu;syntymaaika;sukupuoli;oppijanumero;sukunimi;etunimet;tutkintokoodi;suorituspaivamaara;tutkinnon_taso;aloituspaivamaara;OpiskeluoikeudenAlkamispaivamaara
       |2016;01901;021094-650K;1989-02-01;1;;Nenäkä;Dtes Apu;612101;2016-06-19;2;2011-08-01;2011-08-01
       |2016;01901;281192-654S;1983-04-01;1;1.2.246.562.24.96616592932;Test;Testi Hy;612101;2016-05-31;2;2015-08-01;2015-08-01
       |2016;01901;061188-685J;1991-09-01;2;;Eespä;Eespä Jesta;612101;2016-01-31;2;2014-08-01;2014-08-01
       |2016;01901;291093-711P;1970-10-01;2;;Kaik;Veikee Kaik Aputap;612101;2016-05-18;2;2014-08-01;2014-08-01
       |2016;01901;221195-677D;1981-01-02;3;;Leikkita;Jest Kaikke;612101;2016-03-27;2;2013-08-01;2013-08-01
       |2016;01901;311293-717T;1991-01-02;3;;Sutjaka;Mietis Betat;612101;2016-03-17;2;2014-08-01;2013-08-01
       |2016;01901;260977-606E;1993-01-02;4;;Sutjakast;Ftes Testitap;612101;2016-05-31;4;2014-08-01;2014-08-01
       |2016;01901;;1988-02-02;4;1.2.246.562.24.86863218011;Kai;Betat Testitap;612101;2017-06-06;3;2015-08-01;2015-08-01
       |2016;01901;;;4;1.2.246.562.24.86863218012;Pai;Ketat Testitap;612101;2017-06-06;1;2015-08-01;2015-08-01
       |2016;02358;;;1;;Alho;Aapeli;682601;2018-08-31;;;""".stripMargin

  def createLine(hetu: String = "", oid: String = ""): String =
    s"2016;01901;$hetu;1989-02-01;1;$oid;Nenäkä;Dtes Apu;612101;2016-06-19;2;2011-08-01;2011-08-01"

  def createCsv(line: String, headline: String = defaultHeadline): String =
    s"$headline\n$line"

  def createFile(csvMock: String = mockCsv): File = {
    val file = new File(csvFilePath)
    write(file, csvMock)
    file
  }

  def write(file: File, content: String) = new PrintWriter(file) {
    write(content);
    flush
  }

  def insertAdditionalTestData: Unit = {
    createOrUpdate(slave.henkilö, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis())
    createOrUpdate(etk18vSyntynytToukokuunViimeisenäPäivänä, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis())
    createOrUpdate(etk18vSyntynytKesäkuunEnsimmäisenäPäivänä, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis())
  }

  def verifyAuditLogMessage(henkilö: OppijaHenkilö) = {
    AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> henkilö.oid)))
  }

  def postCsvFile[A](csv: File)(f: => A): A = postFullRequest(csv = Some(csv))(f)

  def postRequest[A](request: TutkintotietoRequest)(f: => A): A = postFullRequest(request = Some(request))(f)

  def postFullRequest[A](request: TutkintotietoRequest, csv: File)(f: => A): A = postFullRequest(request = Some(request), csv = Some(csv))(f)

  def postFullRequest[A](request: Option[TutkintotietoRequest] = None, csv: Option[File] = None)(f: => A): A = {
    post(
      "api/elaketurvakeskus/",
      params = request.fold(Seq.empty[(String, String)])(req => Seq(("json", JsonSerializer.writeWithRoot(req)))),
      files = csv.fold(Seq.empty[(String, Any)])(f => Seq(("csv", f))),
      headers = authHeaders(MockUsers.paakayttaja)
    )(f)
  }
}
