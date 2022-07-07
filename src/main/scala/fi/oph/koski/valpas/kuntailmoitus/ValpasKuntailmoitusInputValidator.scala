package fi.oph.koski.valpas.kuntailmoitus

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.organisaatio.{OrganisaatioRepository, Organisaatiotyyppi}
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.schema.{Oppilaitos, OrganisaatioWithOid, Toimipiste}
import fi.oph.koski.userdirectory.{DirectoryClient, DirectoryUser}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoitusLaajatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntailmoitusInputValidator(
  organisaatioRepository: OrganisaatioRepository,
  valpasRajapäivätService: ValpasRajapäivätService,
  directoryClient: DirectoryClient
) {

  def validateKuntailmoitusInput(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    validateOppijaOid(kuntailmoitusInput)
      .flatMap(validateIlmoituspäivä)
      .flatMap(validateTekijänOid)
      .flatMap(validateKunta)
      .flatMap(fillTekijänHenkilöTiedot)
  }

  private def validateOppijaOid(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot
  ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    kuntailmoitusInput.oppijaOid match {
      case Some(_) => Right(kuntailmoitusInput)
      case _ => Left(ValpasErrorCategory.badRequest.validation.kuntailmoituksenOppijaOid())
    }
  }

  private def validateIlmoituspäivä(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot
  ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    if (valpasRajapäivätService.tarkastelupäivä.isBefore(valpasRajapäivätService.ilmoitustenEnsimmäinenTallennuspäivä)) {
      Left(ValpasErrorCategory.badRequest.validation.kuntailmoituksenIlmoituspäivä())
    } else {
      Right(kuntailmoitusInput)
    }
  }

  private def validateTekijänOid(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    kuntailmoitusInput.tekijä.henkilö match {
      case Some(henkilö) => henkilö.oid match {
        case Some(oid) if oid != user.oid =>
          Left(ValpasErrorCategory.badRequest.validation.kuntailmoituksenTekijä("Kuntailmoitusta ei voi tehdä toisen henkilön oidilla"))
        case _ => Right(kuntailmoitusInput)
      }
      case None => Right(kuntailmoitusInput)
    }
  }

  private def fillTekijänHenkilöTiedot(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot)
                                      (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    directoryClient.findUser(user.username) match {
      case Some(directoryUser) => fillTekijä(user.oid, directoryUser, kuntailmoitusInput)
      case _ => Left(ValpasErrorCategory.internalError("Käyttäjän tietoja ei saatu haettua"))
    }
  }

  private def fillTekijä(tekijäOid: ValpasKuntailmoituksenTekijäHenkilö.Oid,
                         directoryUser: DirectoryUser,
                         kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot
                        ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    val tekijäHenkilö = ValpasKuntailmoituksenTekijäHenkilö(
      oid = Some(tekijäOid),
      etunimet = Some(directoryUser.etunimet),
      sukunimi = Some(directoryUser.sukunimi),
      kutsumanimi = kuntailmoitusInput.tekijä.henkilö.flatMap(_.kutsumanimi),
      email = kuntailmoitusInput.tekijä.henkilö.flatMap(_.email),
      puhelinnumero = kuntailmoitusInput.tekijä.henkilö.flatMap(_.puhelinnumero)
    )

    val kuntailmoitusInputTäydennettynä =
      kuntailmoitusInput.copy(
        tekijä = kuntailmoitusInput.tekijä.copy(
          henkilö = Some(tekijäHenkilö)
       )
      )

    Right(kuntailmoitusInputTäydennettynä)
  }

  private def isAktiivinenKunta(o: OrganisaatioWithOid): Boolean =
    organisaatioRepository.getOrganisaatioHierarkia(o.oid).exists(h =>
      h.aktiivinen && h.organisaatiotyypit.contains(Organisaatiotyyppi.KUNTA)
    )

  private def validateKunta(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedot)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedot] = {
    val virheIlmoitus = Left(
      ValpasErrorCategory.badRequest.validation.kuntailmoituksenKohde(
        s"Kuntailmoituksen kohde ${kuntailmoitusInput.kunta.oid} ei ole aktiivinen kunta"
      ))

    kuntailmoitusInput.kunta match {
      // Tarkistetaan osa suoraan tyypeistä, koska silloin ei tarvitse tehdä hakua organisaatioRepositoryyn
      case _: Oppilaitos => virheIlmoitus
      case _: Toimipiste => virheIlmoitus
      case o: OrganisaatioWithOid if !isAktiivinenKunta(o) => virheIlmoitus
      case o: OrganisaatioWithOid if AhvenanmaanKunnat.onAhvenanmaalainenKunta(o) => Left(
        ValpasErrorCategory.badRequest.validation.kuntailmoituksenKohde(
          s"Kuntailmoituksen kohde ${kuntailmoitusInput.kunta.oid} on ahvenanmaalainen kunta"
        ))
      case _ => Right(kuntailmoitusInput)
    }
  }
}
