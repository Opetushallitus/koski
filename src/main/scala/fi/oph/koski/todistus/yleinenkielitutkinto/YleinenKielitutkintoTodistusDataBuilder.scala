package fi.oph.koski.todistus.yleinenkielitutkinto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Koodistokoodiviite, YleinenKielitutkinto, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.todistus.{TodistusDataValidation, TodistusJob}
import fi.oph.koski.todistus.pdfgenerator.TodistusData
import fi.oph.koski.util.DateOrdering.localDateOptionOrdering

import java.time.LocalDate

class YleinenKielitutkintoTodistusDataBuilder(application: KoskiApplication) {
  private def osasuoritustenJärjestysKoulutusmoduulinTunnisteilla: List[String] = List(
    "puheenymmartaminen",
    "puhuminen",
    "tekstinymmartaminen",
    "kirjoittaminen",
    "rakenteetjasanasto"
  )

  def createTodistusData(
    oppijanHenkilö: OppijaHenkilö,
    ktOo: KielitutkinnonOpiskeluoikeus,
    todistus: TodistusJob
  ): Either[HttpStatus, TodistusData] = {

    for {
      siistittyOo <- poistaArvioimattomatOsasuorituksetJaVanhatArvioinnit(ktOo, todistus)
      yleinenKtSuoritus = siistittyOo.suoritukset.head.asInstanceOf[YleisenKielitutkinnonSuoritus]

      // TODO: TOR-2400: On vielä avoinna, mitä nimeä halutaan käytettävän
      etunimiTodistuksella <- if (oppijanHenkilö.kutsumanimi.nonEmpty) {
        Right(oppijanHenkilö.kutsumanimi)
      } else {
        oppijanHenkilö.etunimet.trim.split(" ").headOption.toRight(
          KoskiErrorCategory.internalError(s"Oppijan etunimi puuttuu todistukselle ${todistus.id}")
        )
      }

      oppijaNimi = s"${etunimiTodistuksella} ${oppijanHenkilö.sukunimi}"

      oppijaSyntymäaika <- oppijanHenkilö.syntymäaika
        .map(formatDateDDMMYYYY)
        .toRight(KoskiErrorCategory.internalError(s"Oppijan syntymäaika puuttuu todistukselle ${todistus.id}"))

      tutkinnonNimi <- formatTutkinnonNimi(yleinenKtSuoritus.koulutusmoduuli, todistus.language)

      suorituksetJaArvosanat <- formatSuorituksetJaArvosanat(yleinenKtSuoritus, todistus)

      tasonArvosanarajat <- formatTasonArvosanarajat(yleinenKtSuoritus.koulutusmoduuli.tunniste, todistus.language)

      toimipisteNimi <- yleinenKtSuoritus.toimipiste.nimi.map(_.get(todistus.language))
        .filter(_.nonEmpty)
        .toRight(KoskiErrorCategory.internalError(s"Toimipisteen nimi puuttuu todistukselle ${todistus.id}"))

      järjestäjäNimi = toimipisteNimi

      ensimmäisenLäsnäTilanAlkupäivä <- ktOo.tila.opiskeluoikeusjaksot.filter(_.tila.koodiarvo == "lasna").headOption
        .map(_.alku)
        .toRight(KoskiErrorCategory.internalError(s"Allekirjoituksen päivä puuttuu todistukselle ${todistus.id}"))

      allekirjoitusPäivämäärä = formatSignatureDate(ensimmäisenLäsnäTilanAlkupäivä, todistus.language)

      vahvistusViimeinenPäivämäärä = formatVahvistusViimeinenPaivamaaraDate(todistus.createdAt.toLocalDate.plusDays(application.config.getLong("todistus.allekirjoituksenVoimassaolonKestoInDays")), todistus.language)

      todistusData = YleinenKielitutkintoTodistusData(
        templateName = s"kielitutkinto_yleinenkielitutkinto_${todistus.language}",
        oppijaNimi = oppijaNimi,
        oppijaSyntymäaika = oppijaSyntymäaika,
        tutkinnonNimi = tutkinnonNimi,
        suorituksetJaArvosanat = suorituksetJaArvosanat.toList,
        tasonArvosanarajat = tasonArvosanarajat,
        järjestäjäNimi = järjestäjäNimi,
        allekirjoitusPäivämäärä = allekirjoitusPäivämäärä,
        vahvistusViimeinenPäivämäärä = vahvistusViimeinenPäivämäärä,
        siistittyOo = siistittyOo
      )

      _ <- TodistusDataValidation.validateYleinenKielitutkintoData(todistusData, todistus.id)
    } yield todistusData
  }

  private def poistaArvioimattomatOsasuorituksetJaVanhatArvioinnit(
    ktOo: KielitutkinnonOpiskeluoikeus,
    todistus: TodistusJob
  ): Either[HttpStatus,KielitutkinnonOpiskeluoikeus] = {
    val yleinenKtSuoritus = ktOo.suoritukset.head.asInstanceOf[YleisenKielitutkinnonSuoritus]

    val uudetOsasuoritukset = yleinenKtSuoritus.osasuoritukset.toList.flatMap(_.flatMap(os => {
      val viimeisinArvosana = os.arviointi.toList.flatten
        .sortBy(_.arviointipäivä)(localDateOptionOrdering).reverse
        .headOption

      viimeisinArvosana.map(arvosana => os.copy(arviointi = Some(List(arvosana))))
    }))

    Either.cond(
      uudetOsasuoritukset.nonEmpty,
      ktOo.copy(suoritukset = List(yleinenKtSuoritus.copy(osasuoritukset = Some(uudetOsasuoritukset)))),
      KoskiErrorCategory.internalError(s"Opiskeluoikeudella ${ktOo.oid.getOrElse("NONE")} ei ole yhtään arvioitua osasuoritusta ${todistus.id}")
    )
  }

  private def formatTutkinnonNimi(tutkinto: YleinenKielitutkinto, language: String): Either[HttpStatus, String] = {
    val kieliKoodi = tutkinto.kieli.koodiarvo
    val tasoKoodi = tutkinto.tunniste.koodiarvo
    val localizationKey = s"todistus:kielitutkinto_yleinenkielitutkinto_tutkinnon_nimi_${kieliKoodi}_${tasoKoodi}"
    getLocalization(localizationKey, language)
  }

  private def formatSuorituksetJaArvosanat(yleinenKtSuoritus: YleisenKielitutkinnonSuoritus, todistus: TodistusJob): Either[HttpStatus, Seq[YleinenKielitutkintoSuoritusJaArvosana]] = {
    HttpStatus.foldEithers(
      yleinenKtSuoritus.osasuoritukset
        .toList.flatten
        .sortBy(os => {
          val index = osasuoritustenJärjestysKoulutusmoduulinTunnisteilla.indexOf(os.koulutusmoduuli.tunniste.koodiarvo)
          if (index >= 0) {
            (0, index, "")
          }
          else {
            (1, Int.MaxValue, os.koulutusmoduuli.tunniste.koodiarvo)
          }
        })
        .map { osasuoritus =>
          val suoritus = osasuoritus.koulutusmoduuli.tunniste.getNimi.map(_.get(todistus.language)).getOrElse("")
          val arvosanaOption = osasuoritus.arviointi.toList.flatten
            .headOption
            .flatMap(_.arvosana.getNimi.map(_.get(todistus.language)))

          arvosanaOption match {
            case Some(arvosana) if arvosana.nonEmpty =>
              Right(YleinenKielitutkintoSuoritusJaArvosana(suoritus, arvosana))
            case _ =>
              Left(KoskiErrorCategory.internalError(s"Arvosana (${arvosanaOption}) tai sen lokalisoitu nimi puuttuu osasuoritukselta todistukselle ${todistus.id}"))
          }
        }
    )
  }

  private def formatTasonArvosanarajat(taso: Koodistokoodiviite, language: String): Either[HttpStatus, String] = {
    val tasoKoodi = taso.koodiarvo
    val localizationKey = s"todistus:kielitutkinto_yleinenkielitutkinto_tason_arvosanarajat_${tasoKoodi}"
    getLocalization(localizationKey, language)
  }

  private def getLocalization(localizationKey: String, language: String): Either[HttpStatus, String] = {
    val localizedString = application.koskiLocalizationRepository.get(localizationKey)
    val translatedValue = localizedString.get(language)

    if (translatedValue.nonEmpty) {
      Right(translatedValue)
    } else {
      Left(KoskiErrorCategory.internalError(s"Lokalisaatio puuttuu tai on tyhjä avaimelle '$localizationKey' kielellä '$language'"))
    }
  }

  private def formatDateDDMMYYYY(date: LocalDate): String = {
    f"${date.getDayOfMonth}%d.${date.getMonthValue}%d.${date.getYear}%04d"
  }

  private def formatSignatureDate(date: LocalDate, language: String): String = {
    language match {
      case "en" =>
        val day = date.getDayOfMonth
        val ordinalSuffix = day match {
          case 1 | 21 | 31 => "st"
          case 2 | 22 => "nd"
          case 3 | 23 => "rd"
          case _ => "th"
        }
        val monthName = date.getMonth.toString.toLowerCase.capitalize
        s"${day}${ordinalSuffix} of ${monthName}, ${date.getYear}"
      case _ => // fi, sv
        formatDateDDMMYYYY(date)
    }
  }

  private def formatVahvistusViimeinenPaivamaaraDate(date: LocalDate, language: String): String = {
    language match {
      case "en" =>
        val day = date.getDayOfMonth
        val monthName = date.getMonth.toString.toLowerCase.capitalize
        s"${day} ${monthName} ${date.getYear}"
      case _ => // fi, sv
        formatDateDDMMYYYY(date)
    }
  }
}
