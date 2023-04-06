package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment
import fi.oph.koski.documentation.{AmmatillinenExampleData, Examples}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.perustiedot.{OpiskeluoikeudenHenkilötiedot, OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SerializationSpec extends AnyFreeSpec with TestEnvironment with Matchers with Logging {
  private implicit val context: ExtractionContext = strictDeserialization
  "Serialization / deserialization" - {
    "Tunnustaminen" in {
      val json = JsonSerializer.serializeWithRoot(AmmatillinenExampleData.tunnustettu)
      val tunnustettu = SchemaValidatingExtractor.extract[OsaamisenTunnustaminen](json).right.get
      tunnustettu should(equal(AmmatillinenExampleData.tunnustettu))
    }

    "Examples" - {
      Examples.oppijaExamples.foreach { example =>
        example.name in {
          val json = JsonSerializer.serializeWithRoot(example.data)
          val oppija = SchemaValidatingExtractor.extract[Oppija](json).right.get
          oppija should(equal(example.data))
          logger.info(example.name + " ok")
        }
      }
    }
    "LocalizedString" - {
      "Serialized/deserializes cleanly" in {
        val string: LocalizedString = LocalizedString.finnish("rölli")
        string.values.foreach { x: AnyRef => {} } // <- force lazy val to evaluate
        val jsonString = JsonSerializer.writeWithRoot(string)
        jsonString should equal("""{"fi":"rölli"}""")
      }
    }

    "Perustiedot" - {
      val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(0, AmmatillinenExampleData.opiskeluoikeus(), OppijaHenkilöWithMasterInfo(henkilö = KoskiSpecificMockOppijat.master, master = None))
      val henkilötiedot = OpiskeluoikeudenHenkilötiedot(perustiedot.id, perustiedot.henkilö.get, perustiedot.henkilöOid)
      "Full" in {
        JsonSerializer.extract[OpiskeluoikeudenOsittaisetTiedot](JsonSerializer.serializeWithRoot(perustiedot)) should equal(perustiedot)
      }
      "Henkilötiedot" in {
        JsonSerializer.extract[OpiskeluoikeudenOsittaisetTiedot](JsonSerializer.serializeWithRoot(henkilötiedot)) should equal(henkilötiedot)
      }
    }

    "Suoritukset" - {

      Examples.oppijaExamples.foreach { e =>
        (e.name + " serialisoituu") in {
          val kaikkiSuoritukset: Seq[Suoritus] = e.data.opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.rekursiivisetOsasuoritukset))
            .filterNot {
              // Suoritukset, jotka riippuvat ylemmällä hierarkiassa olevista kentistä, eivät deserialisoidu oikein yksinään (esim @OnlyWhen-annotaation kautta)
              case _: AikuistenPerusopetuksenOppiaineenSuoritus |
                   _: AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus |
                   _: AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus |
                   _: OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus |
                   _: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus |
                   _: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus |
                   _: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => true
              case s: YhteisenAmmatillisenTutkinnonOsanSuoritus =>  s.osasuoritukset.toList.flatten.exists(_.näyttö.isDefined)
              case s: YhteisenTutkinnonOsanOsaAlueenSuoritus => s.näyttö.isDefined
              case s: DIASuoritus => s.koulutusmoduuli.isInstanceOf[DIAOppiaine]
              case _: LukionOppiaineenSuoritus2015 |
                   _: MuidenLukioOpintojenSuoritus2015 |
                   _: PreIBKurssinSuoritus2015 => true
              case _: LukionOppiaineenSuoritus2019 |
                   _: MuidenLukioOpintojenSuoritus2019 |
                   _: LukionModuulinSuoritus2019 |
                   _: LukionPaikallisenOpintojaksonSuoritus2019 |
                   _: LukionOppiaineenPreIBSuoritus2019 |
                   _: MuidenLukioOpintojenPreIBSuoritus2019 |
                   _: PreIBLukionModuulinSuoritus2019 |
                   _: PreIBLukionPaikallisenOpintojaksonSuoritus2019 => true
              case _: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus |
                   _: VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus |
                   _: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus |
                   _: VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 |
                   _: VSTKotoutumiskoulutuksenAlaosasuoritus2022 => true
              case _: SecondaryLowerOppiaineenSuoritus |
                   _: SecondaryUpperOppiaineenSuoritus => true
              case _ => false
            }

          kaikkiSuoritukset.foreach { s =>
            val jsonString = JsonSerializer.serializeWithRoot(s)
            SchemaValidatingExtractor.extract[Suoritus](jsonString) match {
              case Right(suoritus) => suoritus should (equal(s))
              case Left(error) => fail(s"deserialization of $s failed: $error")
            }
          }
        }
      }
    }

    "Paikallisen koodin koodistouri pudotetaan deserialisoinnissa" in {
      val json = """{"nimi": {"fi": "Paikallinen 1"}, "koodiarvo": "Paikallinen 1", "koodistoUri": "omakoodisto"}"""
      val v = new ValidatingAndResolvingExtractor(MockKoodistoViitePalvelu, MockOrganisaatioRepository)

      v.extract[PaikallinenKoodi](context)(parseJson(json)) match {
        case Right(viite: PaikallinenKoodi) => viite shouldBe PaikallinenKoodi("Paikallinen 1", Finnish("Paikallinen 1"), None)
        case Left(error) => fail(s"deserialization of $json failed: $error")
      }
    }
  }
}
