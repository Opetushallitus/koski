package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.schema.{Oppilaitos, Toimipiste}
import fi.oph.koski.userdirectory.DirectoryUser
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoitusLaajatTiedotJaOppijaOid}
import fi.oph.koski.valpas.valpasuser.ValpasSession

case class ValpasKuntailmoitusInputValidator(application: KoskiApplication) {
  private lazy val organisaatioRepository = application.organisaatioRepository
  private lazy val valpasRajapäivätService = application.valpasRajapäivätService
  private lazy val directoryClient = application.directoryClient

  def validateKuntailmoitusInput(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    validateIlmoituspäivä(kuntailmoitusInput)
      .flatMap(validateTekijänOid)
      .flatMap(validateKunta)
      .flatMap(fillTekijänHenkilöTiedot)
      .flatMap(validateTekijä)
  }

  private def validateIlmoituspäivä(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid
  ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    if (valpasRajapäivätService.tarkastelupäivä.isBefore(valpasRajapäivätService.ilmoitustenEnsimmäinenTallennuspäivä)) {
      Left(ValpasErrorCategory.validation.kuntailmoituksenIlmoituspäivä())
    } else {
      Right(kuntailmoitusInput)
    }
  }

  private def validateTekijänOid(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    kuntailmoitusInput.kuntailmoitus.tekijä.henkilö match {
      case Some(henkilö) => henkilö.oid match {
        case Some(oid) if oid != user.oid =>
          Left(ValpasErrorCategory.validation.kuntailmoituksenTekijä("Kuntailmoitusta ei voi tehdä toisen henkilön oidilla"))
        case _ => Right(kuntailmoitusInput)
      }
      case None => Right(kuntailmoitusInput)
    }
  }

  private def fillTekijänHenkilöTiedot(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
                                      (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    directoryClient.findUser(user.username) match {
      case Some(directoryUser) => fillTekijä(user.oid, directoryUser, kuntailmoitusInput)
      case _ => Left(ValpasErrorCategory.internalError("Käyttäjän tietoja ei saatu haettua"))
    }
  }

  private def fillTekijä(tekijäOid: ValpasKuntailmoituksenTekijäHenkilö.Oid,
                         directoryUser: DirectoryUser,
                         kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid
                        ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    val tekijäHenkilö = ValpasKuntailmoituksenTekijäHenkilö(
      oid = Some(tekijäOid),
      etunimet = Some(directoryUser.etunimet),
      sukunimi = Some(directoryUser.sukunimi),
      kutsumanimi = kuntailmoitusInput.kuntailmoitus.tekijä.henkilö.flatMap(_.kutsumanimi),
      email = kuntailmoitusInput.kuntailmoitus.tekijä.henkilö.flatMap(_.email),
      puhelinnumero = kuntailmoitusInput.kuntailmoitus.tekijä.henkilö.flatMap(_.puhelinnumero)
    )

    val kuntailmoitusInputTäydennettynä =
      kuntailmoitusInput.copy(kuntailmoitus = kuntailmoitusInput.kuntailmoitus.copy(
        tekijä = kuntailmoitusInput.kuntailmoitus.tekijä.copy(
          henkilö = Some(tekijäHenkilö)
        )
      ))

    Right(kuntailmoitusInputTäydennettynä)
  }

  private def validateKunta(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    val virheIlmoitus = Left(
      ValpasErrorCategory.validation.kuntailmoituksenKohde(
        s"Kuntailmoituksen kohde ${kuntailmoitusInput.kuntailmoitus.kunta.oid} ei ole kunta"
      ))

    kuntailmoitusInput.kuntailmoitus.kunta match {
      // Tarkistetaan osa suoraan tyypeistä, koska silloin ei tarvitse tehdä hakua organisaatioRepositoryyn
      case o: Oppilaitos => virheIlmoitus
      case t: Toimipiste => virheIlmoitus
      case _ if !organisaatioRepository.isKunta(kuntailmoitusInput.kuntailmoitus.kunta) => virheIlmoitus
      case _ => Right(kuntailmoitusInput)
    }
  }

  private def validateTekijä(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio match {
      case o: Oppilaitos => Right(kuntailmoitusInput)
      case _ => Left(ValpasErrorCategory.validation.kuntailmoituksenTekijä(
        s"Kuntailmoituksen tekijä ${kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid} ei ole oppilaitos"
      ))
    }
  }
}
