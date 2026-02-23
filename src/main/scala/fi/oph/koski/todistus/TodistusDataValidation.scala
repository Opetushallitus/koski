package fi.oph.koski.todistus

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusOid
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.todistus.yleinenkielitutkinto.{YleinenKielitutkintoSuoritusJaArvosana, YleinenKielitutkintoTodistusData}

 // Validoi todistuksen dataa ennen PDF-generointia ja allekirjoitusta.
 // Koska todistukset ovat virallisia allekirjoitettuja asiakirjoja, tässä
 // varmistetaan että todistukseen ei päädy "outoa" dataa, esim. puuttuvia
 // lokalisaatioavaimia jne.
object TodistusDataValidation {

  def validateYleinenKielitutkintoData(data: YleinenKielitutkintoTodistusData, todistusId: String): Either[HttpStatus, Unit] = {
    for {
      _ <- validateOppijaNimi(data.oppijaNimi, todistusId)
      _ <- validateOppijaSyntymäaika(data.oppijaSyntymäaika, todistusId)
      _ <- validateTutkinnonNimi(data.tutkinnonNimi, todistusId)
      _ <- validateSuorituksetJaArvosanat(data.suorituksetJaArvosanat, todistusId)
      _ <- validateTasonArvosanarajat(data.tasonArvosanarajat, todistusId)
      _ <- validateJärjestäjäNimi(data.järjestäjäNimi, todistusId)
      _ <- validateAllekirjoitusPäivämäärä(data.allekirjoitusPäivämäärä, todistusId)
      _ <- validateOidTunniste(data.oidTunniste, todistusId)
      _ <- validateVahvistusViimeinenPäivämäärä(data.vahvistusViimeinenPäivämäärä, todistusId)
    } yield ()
  }

  private def validateOppijaNimi(nimi: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(nimi, "Oppijan nimi", todistusId)
      .flatMap(_ => validateNotLocalizationKey(nimi, "Oppijan nimi", todistusId))
      .flatMap(_ => validateNotMissingString(nimi, "Oppijan nimi", todistusId))
      .flatMap(_ => validateReasonableLength(nimi, "Oppijan nimi", todistusId, minLength = 1, maxLength = 400))
  }

  private def validateOppijaSyntymäaika(syntymäaika: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(syntymäaika, "Oppijan syntymäaika", todistusId)
      .flatMap(_ => validateNotLocalizationKey(syntymäaika, "Oppijan syntymäaika", todistusId))
      .flatMap(_ => validateNotMissingString(syntymäaika, "Oppijan syntymäaika", todistusId))
      .flatMap(_ => validateReasonableLength(syntymäaika, "Oppijan syntymäaika", todistusId, minLength = 8, maxLength = 10))
  }

  private def validateTutkinnonNimi(tutkinnonNimi: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(tutkinnonNimi, "Tutkinnon nimi", todistusId)
      .flatMap(_ => validateNotLocalizationKey(tutkinnonNimi, "Tutkinnon nimi", todistusId))
      .flatMap(_ => validateNotMissingString(tutkinnonNimi, "Tutkinnon nimi", todistusId))
      .flatMap(_ => validateReasonableLength(tutkinnonNimi, "Tutkinnon nimi", todistusId, minLength = 5, maxLength = 100))
  }

  private def validateSuorituksetJaArvosanat(suoritukset: List[YleinenKielitutkintoSuoritusJaArvosana], todistusId: String): Either[HttpStatus, Unit] = {
    if (suoritukset.isEmpty) {
      return Left(KoskiErrorCategory.internalError(s"Suoritukset ja arvosanat -lista on tyhjä, todistus ${todistusId}"))
    }

    // Validoi jokainen suoritus ja arvosana
    val validationResults = suoritukset.map { suoritusJaArvosana =>
      for {
        _ <- validateNonEmptyNonWhitespace(suoritusJaArvosana.suoritus, "Suorituksen nimi", todistusId)
        _ <- validateNotLocalizationKey(suoritusJaArvosana.suoritus, "Suorituksen nimi", todistusId)
        _ <- validateNotMissingString(suoritusJaArvosana.suoritus, "Suorituksen nimi", todistusId)
        _ <- validateNonEmptyNonWhitespace(suoritusJaArvosana.arvosana, "Arvosana", todistusId)
        _ <- validateNotLocalizationKey(suoritusJaArvosana.arvosana, "Arvosana", todistusId)
        _ <- validateNotMissingString(suoritusJaArvosana.arvosana, "Arvosana", todistusId)
        _ <- validateReasonableLength(suoritusJaArvosana.suoritus, "Suorituksen nimi", todistusId, minLength = 4, maxLength = 50)
        _ <- validateReasonableLength(suoritusJaArvosana.arvosana, "Arvosana", todistusId, minLength = 1, maxLength = 50)
      } yield ()
    }

    HttpStatus.foldEithers(validationResults).map(_ => ())
  }

  private def validateTasonArvosanarajat(arvosanarajat: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(arvosanarajat, "Tason arvosanarajat", todistusId)
      .flatMap(_ => validateNotLocalizationKey(arvosanarajat, "Tason arvosanarajat", todistusId))
      .flatMap(_ => validateNotMissingString(arvosanarajat, "Tason arvosanarajat", todistusId))
      .flatMap(_ => validateReasonableLength(arvosanarajat, "Tason arvosanarajat", todistusId, minLength = 3, maxLength = 20))
  }

  private def validateJärjestäjäNimi(järjestäjäNimi: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(järjestäjäNimi, "Järjestäjän nimi", todistusId)
      .flatMap(_ => validateNotLocalizationKey(järjestäjäNimi, "Järjestäjän nimi", todistusId))
      .flatMap(_ => validateNotMissingString(järjestäjäNimi, "Järjestäjän nimi", todistusId))
      .flatMap(_ => validateReasonableLength(järjestäjäNimi, "Järjestäjän nimi", todistusId, minLength = 5, maxLength = 200))
  }

  private def validateAllekirjoitusPäivämäärä(päivämäärä: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(päivämäärä, "Allekirjoituspäivämäärä", todistusId)
      .flatMap(_ => validateNotLocalizationKey(päivämäärä, "Allekirjoituspäivämäärä", todistusId))
      .flatMap(_ => validateNotMissingString(päivämäärä, "Allekirjoituspäivämäärä", todistusId))
      .flatMap(_ => validateReasonableLength(päivämäärä, "Allekirjoituspäivämäärä", todistusId, minLength = 8, maxLength = 30))
  }

   private def validateOidTunniste(oidTunniste: String, todistusId: String): Either[HttpStatus, Unit] = {
     validateNonEmptyNonWhitespace(oidTunniste, "OidTunniste", todistusId)
       .flatMap(_ => OpiskeluoikeusOid.validateOpiskeluoikeusOid(oidTunniste).map(_ => ()))
   }

  private def validateVahvistusViimeinenPäivämäärä(päivämäärä: String, todistusId: String): Either[HttpStatus, Unit] = {
    validateNonEmptyNonWhitespace(päivämäärä, "Vahvistuksen viimeinen päivämäärä", todistusId)
      .flatMap(_ => validateNotLocalizationKey(päivämäärä, "Vahvistuksen viimeinen päivämäärä", todistusId))
      .flatMap(_ => validateNotMissingString(päivämäärä, "Vahvistuksen viimeinen päivämäärä", todistusId))
      .flatMap(_ => validateReasonableLength(päivämäärä, "Vahvistuksen viimeinen päivämäärä", todistusId, minLength = 8, maxLength = 30))
  }

  // Apufunktiot validointiin

  /**
   * Tarkistaa että merkkijono ei ole tyhjä eikä pelkkää whitespacea
   */
  private def validateNonEmptyNonWhitespace(value: String, fieldName: String, todistusId: String): Either[HttpStatus, Unit] = {
    if (value == null || value.trim.isEmpty) {
      Left(KoskiErrorCategory.internalError(
        s"${fieldName} on tyhjä tai pelkkää whitespacea, todistus ${todistusId}"
      ))
    } else {
      Right(())
    }
  }

  /**
   * Tarkistaa että merkkijono ei ole lokalisaatioavain (eli ei ala "todistus:" eikä sisällä ":")
   */
  private def validateNotLocalizationKey(value: String, fieldName: String, todistusId: String): Either[HttpStatus, Unit] = {
    // Todistus-lokalisaatioavaimet alkavat "todistus:" ja sisältävät useita kaksoispisteitä
    if (value.startsWith("todistus:") || (value.contains(":") && value.split(":").length > 2)) {
      Left(KoskiErrorCategory.internalError(
        s"${fieldName} näyttää olevan lokalisaatioavain eikä käännetty arvo: '${value}', todistus ${todistusId}"
      ))
    } else {
      Right(())
    }
  }

  private def validateNotMissingString(value: String, fieldName: String, todistusId: String): Either[HttpStatus, Unit] = {
    if (value == LocalizedString.missingString) {
      Left(KoskiErrorCategory.internalError(
        s"${fieldName} on '${LocalizedString.missingString}', mikä tarkoittaa että lokalisaatio puuttuu, todistus ${todistusId}"
      ))
    } else {
      Right(())
    }
  }

  private def validateReasonableLength(value: String, fieldName: String, todistusId: String, minLength: Int, maxLength: Int): Either[HttpStatus, Unit] = {
    val length = value.length
    if (length < minLength) {
      Left(KoskiErrorCategory.internalError(
        s"${fieldName} on liian lyhyt (${length} merkkiä, minimi ${minLength}): '${value}', todistus ${todistusId}"
      ))
    } else if (length > maxLength) {
      Left(KoskiErrorCategory.internalError(
        s"${fieldName} on liian pitkä (${length} merkkiä, maksimi ${maxLength}), todistus ${todistusId}"
      ))
    } else {
      Right(())
    }
  }
}
