package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.organisaatio.{OrganisaatioRepository, Organisaatiotyyppi}
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.schema.{Oppilaitos, OrganisaatioWithOid, Toimipiste}
import fi.oph.koski.userdirectory.{DirectoryClient, DirectoryUser}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoituksenTekijäHenkilö, ValpasKuntailmoitusLaajatTiedotJaOppijaOid}
import fi.oph.koski.valpas.valpasuser.ValpasSession

class ValpasKuntailmoitusInputValidator(
  organisaatioRepository: OrganisaatioRepository,
  valpasRajapäivätService: ValpasRajapäivätService,
  directoryClient: DirectoryClient
) {

  def validateKuntailmoitusInput(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    validateIlmoituspäivä(kuntailmoitusInput)
      .flatMap(validateTekijänOid)
      .flatMap(validateKunta)
      .flatMap(fillTekijänHenkilöTiedot)
  }

  private def validateIlmoituspäivä(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid
  ): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    if (valpasRajapäivätService.tarkastelupäivä.isBefore(valpasRajapäivätService.ilmoitustenEnsimmäinenTallennuspäivä)) {
      Left(ValpasErrorCategory.badRequest.validation.kuntailmoituksenIlmoituspäivä())
    } else {
      Right(kuntailmoitusInput)
    }
  }

  private def validateTekijänOid(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
                                (implicit user: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    kuntailmoitusInput.kuntailmoitus.tekijä.henkilö match {
      case Some(henkilö) => henkilö.oid match {
        case Some(oid) if oid != user.oid =>
          Left(ValpasErrorCategory.badRequest.validation.kuntailmoituksenTekijä("Kuntailmoitusta ei voi tehdä toisen henkilön oidilla"))
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

  private def isAktiivinenKunta(o: OrganisaatioWithOid): Boolean =
    organisaatioRepository.getOrganisaatioHierarkia(o.oid).exists(h =>
      h.aktiivinen && h.organisaatiotyypit.contains(Organisaatiotyyppi.KUNTA)
    )

  private def validateKunta(kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid)
  : Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    val virheIlmoitus = Left(
      ValpasErrorCategory.badRequest.validation.kuntailmoituksenKohde(
        s"Kuntailmoituksen kohde ${kuntailmoitusInput.kuntailmoitus.kunta.oid} ei ole aktiivinen kunta"
      ))

    kuntailmoitusInput.kuntailmoitus.kunta match {
      // Tarkistetaan osa suoraan tyypeistä, koska silloin ei tarvitse tehdä hakua organisaatioRepositoryyn
      case _: Oppilaitos => virheIlmoitus
      case _: Toimipiste => virheIlmoitus
      case o: OrganisaatioWithOid if !isAktiivinenKunta(o) => virheIlmoitus
      case o: OrganisaatioWithOid if AhvenanmaanKunnat.onAhvenanmaalainenKunta(o) => Left(
        ValpasErrorCategory.badRequest.validation.kuntailmoituksenKohde(
          s"Kuntailmoituksen kohde ${kuntailmoitusInput.kuntailmoitus.kunta.oid} on ahvenanmaalainen kunta"
        ))
      case _ => Right(kuntailmoitusInput)
    }
  }
}
