package fi.oph.koski.etk

import java.io.File
import java.io.PrintWriter

import fi.oph.koski.api.SharedJetty

protected trait ElaketurvakeskusCLITestMethods {
  private val mockCsvFile =
    """|vuosi;korkeakoulu;hetu;syntymaaika;sukupuoli;oppijanumero;sukunimi;etunimet;tutkintokoodi;suorituspaivamaara;tutkinnon_taso;aloituspaivamaara;OpiskeluoikeudenAlkamispaivamaara
       |2016;01901;021094-650K;1989-02-01;1;;Nenäkä;Dtes Apu;612101;2016-06-19;2;2011-08-01;2011-08-01
       |2016;01901;281192-654S;1983-04-01;1;1.2.246.562.24.96616592932;Test;Testi Hy;612101;2016-05-31;2;2015-08-01;2015-08-01
       |2016;01901;061188-685J;1991-09-01;2;;Eespä;Eespä Jesta;612101;2016-01-31;2;2014-08-01;2014-08-01
       |2016;01901;291093-711P;1970-10-01;2;;Kaik;Veikee Kaik Aputap;612101;2016-05-18;2;2014-08-01;2014-08-01
       |2016;01901;221195-677D;1981-01-02;3;;Leikkita;Jest Kaikke;612101;2016-03-27;2;2013-08-01;2013-08-01
       |2016;01901;311293-717T;1991-01-02;3;;Sutjaka;Mietis Betat;612101;2016-03-17;2;2014-08-01;2013-08-01
       |2016;01901;260977-606E;1993-01-02;4;;Sutjakast;Ftes Testitap;612101;2016-05-31;2;2014-08-01;2014-08-01
       |2016;01901;;1988-02-02;4;1.2.246.562.24.86863218011;Kai;Betat Testitap;612101;2017-06-06;2;2015-08-01;2015-08-01""".stripMargin

  val koskiPort = SharedJetty.port.toString
  val csvFilePath = "csv-tiedosto-testia-varten.csv"
  val failingCsvFilePath = "csv-tiedosto-testia-varten-failing.csv"

  def withCsvFixture(f: => Unit) = {
    val file = new File(csvFilePath)
    write(file, mockCsvFile)
    (f)
    file.delete
  }

  def withFailingCsvFixture(f: => Unit) = {
    val file = new File(failingCsvFilePath)
    write(file, mockCsvFile.replace(";;", ";"))
    (f)
    file.delete
  }

  private def write(file: File, content: String) = new PrintWriter(file){ write(content); flush }
}

protected trait MockOutput extends Output {
  var consoleOutput = ""

  override def printResult(s: String): Unit = consoleOutput = s
}
