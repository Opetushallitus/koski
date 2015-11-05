package fi.oph.tor.schema

import java.time.LocalDate.{of => date}

import fi.oph.tor.json.Json
import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.reflect.TypeInfo

object SchemaTest extends App {
  val oppija = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpintoOikeus(
        Some(983498343),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Some(Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
        Suoritus(
          Koulutustoteutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4),
            Some("39/011/2014"),
            Some(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2)),
            Some(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3))
          ),
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)),
          Some(KoodistoKoodiViite("valmis", Some("Valmis"), "suorituksentila", 1)),
          alkamispäivä = None,
          arviointi = None,
          Some(Vahvistus(Some(date(2016, 1, 9)))),
          Some(List(
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2012, 10, 20)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2013, 1, 31)))),
              osasuoritukset = None
            ),
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("100018", Some("Pintavauriotyöt"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2013, 3, 20)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2013, 5, 31)))),
              osasuoritukset = None
            ),
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2013, 4, 1)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2013, 5, 31)))),
              osasuoritukset = None
            ),
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2014, 10, 20)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2014, 11, 8)))),
              osasuoritukset = None
            ),
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2015, 4, 1)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2015, 5, 1)))),
              osasuoritukset = None
            ),
            Suoritus(
              Tutkinnonosatoteutus(
                Some(KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", 1)),
                paikallinenKoodi = None,
                None,
                Some(true)
              ),
              suorituskieli = None,
              Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
                Some(date(2016, 2, 1)),
                arvosananKorottaminen = None
              )),
              Some(Vahvistus(Some(date(2016, 5, 1)))),
              osasuoritukset = None
            )
          ))
        ),
        hojks = None,
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "tavoite", 1)),
        Some(Läsnäolotiedot(List(
          Läsnäolojakso(date(2012, 9, 1), Some(date(2013, 5, 31)), KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolo", 1)),
          Läsnäolojakso(date(2013, 9, 1), Some(date(2014, 5, 31)), KoodistoKoodiViite("poissa", Some("Poissa"), "lasnaolo", 1)),
          Läsnäolojakso(date(2014, 9, 1), Some(date(2015, 5, 31)), KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolo", 1)),
          Läsnäolojakso(date(2015, 9, 1), None, KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolo", 1))
        ))),
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", 1))
      )
    )
  )
  println(Json.writePretty(oppija))
}

class KoulutusmoduulitoteutusSerializer extends Serializer[Koulutusmoduulitoteutus] {
  private val TheClass = classOf[Koulutusmoduulitoteutus]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Koulutusmoduulitoteutus] = {
    case (TypeInfo(TheClass, _), json) => json match {
      case moduuli: JObject if moduuli.values.contains("koulutuskoodi") => moduuli.extract[Koulutustoteutus]
      case moduuli: JObject if moduuli.values.contains("tutkinnonosakoodi") => moduuli.extract[Tutkinnonosatoteutus]
      case moduuli: JObject => throw new RuntimeException("Unknown Koulutusmoduulitoteutus" + json)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}