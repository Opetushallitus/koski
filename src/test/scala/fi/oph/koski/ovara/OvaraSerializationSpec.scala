package fi.oph.koski.ovara

import fi.oph.koski.json.JsonSerializer
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OvaraSerializationSpec extends AnyFreeSpec with Matchers {
  "OvaraOpiskelijavalintatieto" - {
    "deserialisoituu oikein JSON:sta" in {
      val json =
        """{
          |  "oppijanumero" : "1.2.246.562.24.98249516231",
          |  "hetu" : "1.2.246.562.24.98249516231",
          |  "syntymaaika" : "241164-927S",
          |  "sukunimi" : "1964-11-24",
          |  "etunimet" : "Räsänen-Testi",
          |  "hakemukset" : [ {
          |    "hakemusOid" : "1.2.246.562.11.00000000000002257807",
          |    "haku" : {
          |      "oid" : "1.2.246.562.29.00000000000000036124",
          |      "nimi" : {
          |        "fi" : "Korkeakoulujen kevään 2024 toinen yhteishaku ",
          |        "sv" : "Högskolornas andra gemensamma ansökan, våren 2024",
          |        "en" : "Joint Application to Degree Programmes in Finnish/Swedish, Spring 2024"
          |      }
          |    },
          |    "haunKohdejoukko" : "haunkohdejoukko_12#1",
          |    "hakutapa" : "hakutapa_01#1",
          |    "hakutoiveet" : [ {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000041008",
          |        "nimi" : {
          |          "fi" : "Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta",
          |          "sv" : "Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta",
          |          "en" : "Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.80037732585",
          |        "nimi" : {
          |          "fi" : "Lääketieteen ja terveysteknologian tiedekunta",
          |          "sv" : "Lääketieteen ja terveysteknologian tiedekunta"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2024",
          |      "valinnanTila" : null,
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000038548",
          |        "nimi" : {
          |          "fi" : "Bioinformaatioteknologia, tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta",
          |          "sv" : "Bioinformationsteknologi, teknologie kandidat och diplomingenjör (3 år + 2 år) - DIA gemensamma antagning",
          |          "en" : "Bioinformaatioteknologia, tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.38864316104",
          |        "nimi" : {
          |          "fi" : "Sähkötekniikan korkeakoulu",
          |          "sv" : "Högskolan för elektroteknik"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2024",
          |      "valinnanTila" : null,
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000038252",
          |        "nimi" : {
          |          "fi" : "Tietojenkäsittelytieteen kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Kandidatprogrammet i datavetenskap, kandidat i naturvetenskaper och filosofie magister (3 år + 2 år)",
          |          "en" : "Tietojenkäsittelytieteen kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.94639300915",
          |        "nimi" : {
          |          "fi" : "Matemaattis-luonnontieteellinen tiedekunta",
          |          "sv" : "Matematisk-naturvetenskapliga fakulteten"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2024",
          |      "valinnanTila" : null,
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    } ]
          |  }, {
          |    "hakemusOid" : "1.2.246.562.11.00000000000002730661",
          |    "haku" : {
          |      "oid" : "1.2.246.562.29.00000000000000054531",
          |      "nimi" : {
          |        "fi" : "Korkeakoulujen kevään 2025 toinen yhteishaku ",
          |        "sv" : "Högskolornas andra gemensamma ansökan, våren 2025",
          |        "en" : "Joint Application to Degree Programmes in Finnish/Swedish, Spring 2025"
          |      }
          |    },
          |    "haunKohdejoukko" : "haunkohdejoukko_12#1",
          |    "hakutapa" : "hakutapa_01#1",
          |    "hakutoiveet" : [ {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000056371",
          |        "nimi" : {
          |          "fi" : "Biologian kandiohjelma (suomen kielen taitoisille hakijoille), luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Biologian kandiohjelma (suomen kielen taitoisille hakijoille), luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "en" : "Biologian kandiohjelma (suomen kielen taitoisille hakijoille), luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.41941575486",
          |        "nimi" : {
          |          "fi" : "Bio- ja ympäristötieteellinen tiedekunta",
          |          "sv" : "Bio- och miljövetenskapliga fakulteten"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2025",
          |      "valinnanTila" : "HYVAKSYTTY",
          |      "vastaanotonTila" : "EI_VASTAANOTETTU_MAARA_AIKANA",
          |      "ilmoittautumisenTila" : "EI_ILMOITTAUTUNUT"
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000056851",
          |        "nimi" : {
          |          "fi" : "Biologia,  luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Biologia,  luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "en" : "Biologia,  luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.42196752952",
          |        "nimi" : {
          |          "fi" : "Matemaattis-luonnontieteellinen tiedekunta",
          |          "sv" : "Matemaattis-luonnontieteellinen tiedekunta"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2025",
          |      "valinnanTila" : null,
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000056373",
          |        "nimi" : {
          |          "fi" : "Ympäristötieteiden kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Kandidatprogrammet i miljövetenskaper, kandidat i naturvetenskaper och filosofie magister (3 år + 2 år)",
          |          "en" : "Ympäristötieteiden kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.41941575486",
          |        "nimi" : {
          |          "fi" : "Bio- ja ympäristötieteellinen tiedekunta",
          |          "sv" : "Bio- och miljövetenskapliga fakulteten"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2025",
          |      "valinnanTila" : null,
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000056370",
          |        "nimi" : {
          |          "fi" : "Molekyylibiotieteiden kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Kandidatprogrammet i molekylära biovetenskaper, kandidat i naturvetenskaper och filosofie magister (3 år + 2 år)",
          |          "en" : "Molekyylibiotieteiden kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.41941575486",
          |        "nimi" : {
          |          "fi" : "Bio- ja ympäristötieteellinen tiedekunta",
          |          "sv" : "Bio- och miljövetenskapliga fakulteten"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2025",
          |      "valinnanTila" : "PERUUNTUNUT",
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    }, {
          |      "hakukohde" : {
          |        "oid" : "1.2.246.562.20.00000000000000056422",
          |        "nimi" : {
          |          "fi" : "Kemian kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)",
          |          "sv" : "Kandidatprogrammet i kemi, kandidat i naturvetenskaper och filosofie magister (3 år + 2 år)",
          |          "en" : "Kemian kandiohjelma, luonnontieteiden kandidaatti ja filosofian maisteri (3 v + 2 v)"
          |        }
          |      },
          |      "tarjoaja" : {
          |        "oid" : "1.2.246.562.10.94639300915",
          |        "nimi" : {
          |          "fi" : "Matemaattis-luonnontieteellinen tiedekunta",
          |          "sv" : "Matematisk-naturvetenskapliga fakulteten"
          |        }
          |      },
          |      "koulutuksenAlkamiskausiUri" : "kausi_s#1",
          |      "koulutuksenAlkamisvuosi" : "2025",
          |      "valinnanTila" : "PERUUNTUNUT",
          |      "vastaanotonTila" : null,
          |      "ilmoittautumisenTila" : null
          |    } ]
          |  } ]
          |}""".stripMargin

      val valintatieto = JsonSerializer.parse[OvaraOpiskelijavalintatieto](json)
      valintatieto.oppijanumero should be("1.2.246.562.24.98249516231")
      valintatieto.hetu should be(Some("1.2.246.562.24.98249516231"))
      valintatieto.syntymaaika should be(Some("241164-927S"))
      valintatieto.sukunimi should be(Some("1964-11-24"))
      valintatieto.etunimet should be(Some("Räsänen-Testi"))
      valintatieto.hakemukset should have length 2

      val hakemus1 = valintatieto.hakemukset.head
      hakemus1.hakemusOid should be("1.2.246.562.11.00000000000002257807")
      hakemus1.haku.oid should be("1.2.246.562.29.00000000000000036124")
      hakemus1.haku.nimi.fi should be(Some("Korkeakoulujen kevään 2024 toinen yhteishaku "))
      hakemus1.haku.nimi.sv should be(Some("Högskolornas andra gemensamma ansökan, våren 2024"))
      hakemus1.haku.nimi.en should be(Some("Joint Application to Degree Programmes in Finnish/Swedish, Spring 2024"))
      hakemus1.haunKohdejoukko should be(Some("haunkohdejoukko_12#1"))
      hakemus1.hakutapa should be(Some("hakutapa_01#1"))
      hakemus1.hakutoiveet should have length 3

      val hakutoive1 = hakemus1.hakutoiveet.head
      hakutoive1.hakukohde.oid should be("1.2.246.562.20.00000000000000041008")
      hakutoive1.hakukohde.nimi.fi should be (Some("Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta"))
      hakutoive1.hakukohde.nimi.sv should be (Some("Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta"))
      hakutoive1.hakukohde.nimi.en should be (Some("Bioteknologian ja biolääketieteen tekniikan koulutus, Tekniikan kandidaatti ja diplomi-insinööri (3 v + 2 v) - DIA-yhteisvalinta"))
      hakutoive1.tarjoaja.get.oid should be("1.2.246.562.10.80037732585")
      hakutoive1.tarjoaja.get.nimi.fi should be(Some("Lääketieteen ja terveysteknologian tiedekunta"))
      hakutoive1.tarjoaja.get.nimi.sv should be(Some("Lääketieteen ja terveysteknologian tiedekunta"))
      hakutoive1.tarjoaja.get.nimi.en should be(None)
      hakutoive1.koulutuksenAlkamiskausiUri should be(Some("kausi_s#1"))
      hakutoive1.koulutuksenAlkamisvuosi should be(Some("2024"))
      hakutoive1.valinnanTila should be(None)
      hakutoive1.vastaanotonTila should be(None)
      hakutoive1.ilmoittautumisenTila should be(None)

      val hakemus2 = valintatieto.hakemukset(1)
      hakemus2.hakemusOid should be("1.2.246.562.11.00000000000002730661")
      hakemus2.haku.oid should be ("1.2.246.562.29.00000000000000054531")
      hakemus2.haku.nimi.fi should be (Some("Korkeakoulujen kevään 2025 toinen yhteishaku "))
      hakemus2.haku.nimi.sv should be (Some("Högskolornas andra gemensamma ansökan, våren 2025"))
      hakemus2.haku.nimi.en should be (Some("Joint Application to Degree Programmes in Finnish/Swedish, Spring 2025"))
      hakemus2.haunKohdejoukko should be(Some("haunkohdejoukko_12#1"))
      hakemus2.hakutapa should be(Some("hakutapa_01#1"))
      hakemus2.hakutoiveet should have length 5

      val hakutoive2 = hakemus2.hakutoiveet.head
      hakutoive2.valinnanTila should be(Some("HYVAKSYTTY"))
      hakutoive2.vastaanotonTila should be(Some("EI_VASTAANOTETTU_MAARA_AIKANA"))
      hakutoive2.ilmoittautumisenTila should be(Some("EI_ILMOITTAUTUNUT"))
    }
  }
}
