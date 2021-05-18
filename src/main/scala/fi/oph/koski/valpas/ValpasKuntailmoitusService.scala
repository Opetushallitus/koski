package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, OidOrganisaatio, OrganisaatioWithOid}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppilaitos
import fi.oph.koski.valpas.valpasrepository._
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.yhteystiedot.{ValpasYhteystiedot, ValpasYhteystietoHakemukselta, ValpasYhteystietoOppijanumerorekisteristä}

class ValpasKuntailmoitusService(
  application: KoskiApplication
) extends Logging with Timing {
  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)
  private val repository = application.valpasKuntailmoitusRepository
  private val oppijaService = application.valpasOppijaService
  private val directoryClient = application.directoryClient
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val koodistoViitePalvelu = application.koodistoViitePalvelu
  private val organisaatioService = application.organisaatioService

  def createKuntailmoitus(
    kuntailmoitusInput: ValpasKuntailmoitusLaajatTiedotJaOppijaOid
  )(implicit session: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusLaajatTiedotJaOppijaOid] = {
    val organisaatioOid = kuntailmoitusInput.kuntailmoitus.tekijä.organisaatio.oid

    accessResolver.assertAccessToOrg(organisaatioOid).left.map(_ =>
      ValpasErrorCategory.forbidden.organisaatio(
        "Käyttäjällä ei ole oikeutta tehdä kuntailmoitusta annetun organisaation nimissä"
      ))
      .flatMap(_ => oppijaService.getOppijaLaajatTiedot(kuntailmoitusInput.oppijaOid))
      .flatMap(oppija =>
        accessResolver.withOppijaAccessAsOrganisaatio(organisaatioOid)(oppija)
          .left.map(_ => ValpasErrorCategory.forbidden.oppija(
            "Käyttäjällä ei ole oikeuksia tehdä kuntailmoitusta annetusta oppijasta"
          ))
      )
      .flatMap(_ => repository.create(kuntailmoitusInput))
  }

  def haePohjatiedot(
    pohjatiedotInput: ValpasKuntailmoitusPohjatiedotInput
  )(implicit session: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusPohjatiedot] = {
    haeDirectoryKäyttäjä
      .flatMap(täydennäOppijanumerorekisterinTiedoilla)
      .map(teePohjatiedot)
      .map(täydennäKunnilla)
      .map(täydennäMailla)
      .map(täydennäYhteydenottokielillä)
      .flatMap(täydennäOppijoidenTiedoilla(pohjatiedotInput))
      .map(täydennäTekijäorganisaatioilla)
  }

  private def haeDirectoryKäyttäjä(implicit session: ValpasSession): Either[HttpStatus, ValpasKuntailmoituksenTekijäHenkilö] =
    directoryClient.findUser(session.username) match {
      case Some(directoryUser) =>
        Right(ValpasKuntailmoituksenTekijäHenkilö(
          oid = Some(session.oid),
          etunimet = Some(directoryUser.etunimet),
          sukunimi = Some(directoryUser.sukunimi),
          kutsumanimi = None,
          email = None,
          puhelinnumero = None
        ))
      case _ => Left(ValpasErrorCategory.internalError("Käyttäjän tietoja ei saatu haettua"))
    }

  private def täydennäOppijanumerorekisterinTiedoilla(
    tekijä: ValpasKuntailmoituksenTekijäHenkilö
  ): Either[HttpStatus, ValpasKuntailmoituksenTekijäHenkilö] =
    oppijanumerorekisteri.findOppijaJaYhteystiedotByOid(tekijä.oid.get) match {
      case Some(laajatOppijaHenkilöTiedot) =>
        Right(täydennäTekijänTiedoilla(tekijä, laajatOppijaHenkilöTiedot))
      case _ =>
        Right(tekijä)
    }

  private def täydennäTekijänTiedoilla(
    tekijä: ValpasKuntailmoituksenTekijäHenkilö,
    laajatOppijaHenkilöTiedot: LaajatOppijaHenkilöTiedot
  ): ValpasKuntailmoituksenTekijäHenkilö = {
    val työosoitteet = laajatOppijaHenkilöTiedot.yhteystiedot.filter(_.tyyppi.koodiarvo == "yhteystietotyyppi2") // "Työosoite"
    val emails: Seq[String] = työosoitteet
      .filter(_.sähköposti.isDefined)
      .flatMap(_.sähköposti)
      .map(_.trim)
      .filter(_ != "")

    val email = if (emails.isEmpty) None else Some(emails.toSet.toSeq.sorted.mkString(", "))

    val puhelinnumerot: Seq[String] = työosoitteet
      .flatMap(o => Seq(o.matkapuhelinnumero, o.puhelinnumero))
      .flatten
      .map(_.trim)
      .filter(_ != "")

    val puhelinnumero = if (puhelinnumerot.isEmpty) None else Some(puhelinnumerot.toSet.toSeq.sorted.mkString(", "))

    tekijä.copy(
      kutsumanimi = Some(laajatOppijaHenkilöTiedot.kutsumanimi),
      email = email,
      puhelinnumero = puhelinnumero
    )
  }

  private def teePohjatiedot(tekijäHenkilö: ValpasKuntailmoituksenTekijäHenkilö): ValpasKuntailmoitusPohjatiedot = {
    ValpasKuntailmoitusPohjatiedot(
      tekijäHenkilö = Some(tekijäHenkilö)
    )
  }

  private def täydennäKunnilla(
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(implicit session: ValpasSession): ValpasKuntailmoitusPohjatiedot = {
    pohjatiedot.copy(
      kunnat = organisaatioService.kunnat.map(oh => OidOrganisaatio(oh.oid, Some(oh.nimi), oh.kotipaikka))
    )
  }

  private def täydennäMailla(
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(implicit session: ValpasSession): ValpasKuntailmoitusPohjatiedot = {
    val koodistoUri = "maatjavaltiot2"
    val koodisto = koodistoViitePalvelu.getLatestVersionOptional(koodistoUri)
    val maat = koodisto match {
      case Some(koodisto) => koodistoViitePalvelu.getKoodistoKoodiViitteet(koodisto)
      case _ => {
        logger.warn("Koodistoa ei löydy koodistopalvelusta: " + koodistoUri)
        Seq.empty
      }
    }

    pohjatiedot.copy(maat = maat)
  }

  private def täydennäYhteydenottokielillä(
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(implicit session: ValpasSession): ValpasKuntailmoitusPohjatiedot = {
    pohjatiedot.copy(
      yhteydenottokielet = tuetutYhteydenottokielet
    )
  }

  private def tuetutYhteydenottokielet: Seq[Koodistokoodiviite] = Seq(
    Koodistokoodiviite("FI", "kieli"),
    Koodistokoodiviite("SV", "kieli")
  ).map(koodistoViitePalvelu.validate).flatten

  private def täydennäOppijoidenTiedoilla(pohjatiedotInput: ValpasKuntailmoitusPohjatiedotInput)(
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(implicit session: ValpasSession): Either[HttpStatus, ValpasKuntailmoitusPohjatiedot] = {
    haeOppijat(pohjatiedotInput)
      .flatMap(tarkistaOikeudetJaJärjestäOppijat(pohjatiedotInput))
      .map(täydennäPohjatiedotOppijoidenTiedoilla(pohjatiedotInput.tekijäOrganisaatio, pohjatiedot))
  }

  private def haeOppijat(
    pohjatiedotInput: ValpasKuntailmoitusPohjatiedotInput
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    pohjatiedotInput.tekijäOrganisaatio match {
      case Some(oppilaitos) => haeOppilaitoksenOppijat(oppilaitos.oid, pohjatiedotInput.oppijaOidit)
      case _ => haeYksittäisetOppijat(pohjatiedotInput.oppijaOidit)
    }
  }

  private def haeOppilaitoksenOppijat(
    oppilaitosOid: ValpasOppilaitos.Oid, oppijaOidit: Seq[String]
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    oppijaService.getOppijatLaajatTiedotYhteystiedoillaJaLisätiedoilla(oppilaitosOid, oppijaOidit)
  }

  private def haeYksittäisetOppijat(
    oppijaOidit: Seq[String]
  )(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    // Tämä hakeminen aiheuttaa monta SQL-queryä. Tätä voisi optimoida, mutta käytännössä tähän metodiin ei toistaiseksi
    // koskaan päädytä kuin yhden oppijan näkymästä, koska listanäkymässä ilmoituksia tehtäessä tekijän
    // oppilaitos on aina tiedossa.
    HttpStatus.foldEithers(oppijaOidit.map(oppijaOid => oppijaService.getOppijaHakutilanteillaLaajatTiedotJaLisätiedot(oppijaOid)).toSeq)
  }

  private def tarkistaOikeudetJaJärjestäOppijat(pohjatiedotInput: ValpasKuntailmoitusPohjatiedotInput)(
    oppijatHakutilanteilla: Seq[OppijaHakutilanteillaLaajatTiedot]
  ): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    def lessThanInputinJärjestyksenMukaan(
      a: OppijaHakutilanteillaLaajatTiedot,
      b: OppijaHakutilanteillaLaajatTiedot
    ): Boolean = {
      pohjatiedotInput.oppijaOidit.indexOf(a.oppija.henkilö.oid) < pohjatiedotInput.oppijaOidit.indexOf(b.oppija.henkilö.oid)
    }

    // Tarkista, että jokainen inputtina annettu oppija löytyy saadulta listalta samalla oppija-oidilla. Oppijoiden
    // master-slave-oideja ei tässä siis käsitellä, vaan palautetaan vaan virhe, jos kysely on tehty slave oidilla.
    // Tämä ei käytännössä haittaa, koska käyttöliittymä perustuu aina master-oideina palautettuun oppija-dataan.
    val oppijaOidsTietokannasta = oppijatHakutilanteilla.map(_.oppija.henkilö.oid)
    if (pohjatiedotInput.oppijaOidit.toSet == oppijaOidsTietokannasta.toSet) {
      // Järjestä oppijat samaan järjestykseen kuin pyynnössä. Melko tehoton sorttaus, mutta dataa ei ole paljon.
      Right(oppijatHakutilanteilla.sortWith(lessThanInputinJärjestyksenMukaan))
    } else {
      Left(ValpasErrorCategory.forbidden.oppijat("Käyttäjällä ei ole oikeuksia kaikkien oppijoiden tietoihin"))
    }
  }

  private def täydennäPohjatiedotOppijoidenTiedoilla(
    tekijäOrganisaatio: Option[OrganisaatioWithOid],
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(
    oppijat: Seq[OppijaHakutilanteillaLaajatTiedot]
  )(implicit session: ValpasSession): ValpasKuntailmoitusPohjatiedot = {
    val täydennetytOppijoidenPohjatiedot = oppijat.map(oppija => {
      val uudetPohjatietojenYhteystiedot =
        oppija.yhteystiedot.map(yhteystiedotOppijanYhteystiedoista(pohjatiedot.maat, pohjatiedot.kunnat))

      ValpasOppijanPohjatiedot(
        oppijaOid =
          oppija.oppija.henkilö.oid,
        mahdollisetTekijäorganisaatiot =
          mahdollisetTekijäorganisaatiot(tekijäOrganisaatio, oppija.oppija.oikeutetutOppilaitokset),
        yhteydenottokieli =
          yhteydenottokieli(oppija.oppija.henkilö.äidinkieli),
        turvakielto =
          oppija.oppija.henkilö.turvakielto,
        yhteystiedot =
          järjestäYhteystiedot(uudetPohjatietojenYhteystiedot),
        hetu = oppija.oppija.henkilö.hetu,
      )
    })

    pohjatiedot.copy(oppijat = täydennetytOppijoidenPohjatiedot)
  }

  private def yhteystiedotOppijanYhteystiedoista(
    maat: Seq[Koodistokoodiviite],
    kunnat: Seq[OrganisaatioWithOid],
  )(
    valpasYhteystiedot: ValpasYhteystiedot
  ): ValpasPohjatietoYhteystieto = {
    val maa = maaOppijanYhteystiedoista(maat, valpasYhteystiedot)
    val kunta = kuntaOppijanYhteystiedoista(kunnat, valpasYhteystiedot)

    ValpasPohjatietoYhteystieto(
      yhteystietojenAlkuperä = valpasYhteystiedot.alkuperä,
      yhteystiedot = ValpasKuntailmoituksenOppijanYhteystiedot(
        puhelinnumero = valpasYhteystiedot.matkapuhelinnumero.orElse(valpasYhteystiedot.puhelinnumero),
        email = valpasYhteystiedot.sähköposti,
        lähiosoite = valpasYhteystiedot.lähiosoite,
        postinumero = valpasYhteystiedot.postinumero,
        postitoimipaikka = valpasYhteystiedot.postitoimipaikka,
        maa = maa
      ),
      kunta = kunta
    )
  }

  private def maaOppijanYhteystiedoista(
    maat: Seq[Koodistokoodiviite],
    yhteystiedot: ValpasYhteystiedot
  ): Option[Koodistokoodiviite] = {
    val haluttuMaanNimiTrimmattuna: Option[String] = yhteystiedot.maa.map(_.trim.toLowerCase)

    val maa: Option[Koodistokoodiviite] = (haluttuMaanNimiTrimmattuna match {
      case Some("") => None // Varmuuden vuoksi hylätään kokonaan tyhjä maan nimi, koska jonkin maan nimi
      // jollakin tuetulla kielellä koodistossa saattaa olla tyhjä
      case Some(haluttuMaanNimiTrimmattuna) => maat.find(maa => maa.nimi match {
        case Some(koodistoMaanNimi) if koodistoMaanNimi.get("fi").toLowerCase == haluttuMaanNimiTrimmattuna => true
        case Some(koodistoMaanNimi) if koodistoMaanNimi.get("sv").toLowerCase == haluttuMaanNimiTrimmattuna => true
        case Some(koodistoMaanNimi) if koodistoMaanNimi.get("en").toLowerCase == haluttuMaanNimiTrimmattuna => true
        case _ => false
      })
      case _ => None
    }).orElse(oletusMaa)
    maa
  }

  private def oletusMaa: Option[Koodistokoodiviite] =
    koodistoViitePalvelu.validate(Koodistokoodiviite("246", "maatjavaltiot2")) // Suomi

  private def kuntaOppijanYhteystiedoista(
    kunnat: Seq[OrganisaatioWithOid],
    valpasYhteystiedot: ValpasYhteystiedot
  ): Option[OrganisaatioWithOid] = {
    // Yritetään päätellä kunta ensimmäisenä listalla olevan yhteystiedon perusteella. Jos oppijalla on aktiivisten
    // hakemusten yhteystietoja, se on ensimmäisenä, muussa tapauksessa ensimmäisenä on ensimmäinen
    // oppijanumerorekisteristä saatu DVV:n yhteystieto.
    // Tätä voisi parantaa mm. käyttämällä postinumerohakua, jos maa on Suomi.
    val postitoimipaikkaTrimmattuna: Option[String] = valpasYhteystiedot.postitoimipaikka.map(_.trim.toLowerCase)

    val kunta: Option[OrganisaatioWithOid] = postitoimipaikkaTrimmattuna match {
      case Some("") => None // Varmuuden vuoksi hylätään kokonaan tyhjä postitoimipaikka, koska jonkin kunnan
      // nimi jollain tuetulla kielellä saattaa olla tyhjä koodistossa
      case Some(postitoimipaikkaTrimmattuna) => {
        kunnat.find(_.kotipaikka.flatMap(_.nimi) match {
          case Some(kotipaikanNimi) if kotipaikanNimi.get("fi").toLowerCase == postitoimipaikkaTrimmattuna => true
          case Some(kotipaikanNimi) if kotipaikanNimi.get("sv").toLowerCase == postitoimipaikkaTrimmattuna => true
          case _ => false
        }
        )
      }
      case _ => None
    }
    kunta
  }

  private def mahdollisetTekijäorganisaatiot(
    tekijäOrganisaatio: Option[OrganisaatioWithOid],
    oppijanOppilaitokset: Set[ValpasOppilaitos.Oid]
  )(implicit session: ValpasSession): Seq[ValpasTekijäorganisaationPohjatiedot] = {
    tekijäOrganisaatio match {
      // Jos organisaatio on annettu, ei palauteta toistaiseksi mitään muita vaihtoehtoja,
      // vaikka jollekin yksittäiselle oppijalle voisikin lähettää ilmoituksen muusta oppilaitoksesta käsin.
      case Some(o) if accessResolver.filterByOikeudet(Set(o.oid)) == Set(o.oid) => Seq(ValpasTekijäorganisaationPohjatiedot(o, None)) // TODO: listää hakenutMuualle
      case Some(o) => Seq.empty
      case _ => accessResolver.filterByOikeudet(oppijanOppilaitokset)
        .toSeq.map(oid => ValpasTekijäorganisaationPohjatiedot(OidOrganisaatio(oid = oid), None)) // TODO: lisää hakenutMuualle
    }
  }

  private def yhteydenottokieli(äidinkieli: Option[String]): Option[Koodistokoodiviite] = {
    val koodistoviite = äidinkieli match {
      case Some("sv") => Koodistokoodiviite("SV", "kieli")
      case _ => Koodistokoodiviite("FI", "kieli")
    }
    koodistoViitePalvelu.validate(koodistoviite)
  }

  private def järjestäYhteystiedot(yhteystiedot: Seq[ValpasPohjatietoYhteystieto]): Seq[ValpasPohjatietoYhteystieto] = {
    val hakemusYhteystiedot = yhteystiedot.filter(_.yhteystietojenAlkuperä.isInstanceOf[ValpasYhteystietoHakemukselta])
    val oppijanumerorekisterinYhteystiedot =
      yhteystiedot.filter(_.yhteystietojenAlkuperä.isInstanceOf[ValpasYhteystietoOppijanumerorekisteristä])
    val muutYhteystiedot = yhteystiedot.diff(hakemusYhteystiedot ++ oppijanumerorekisterinYhteystiedot)

    hakemusYhteystiedot ++ oppijanumerorekisterinYhteystiedot ++ muutYhteystiedot
  }

  private def täydennäTekijäorganisaatioilla(
    pohjatiedot: ValpasKuntailmoitusPohjatiedot
  )(implicit session: ValpasSession): ValpasKuntailmoitusPohjatiedot = {
    val kaikissaOppijoissaEsiintyvätMahdollisetTekijäorganisaatiot =
      pohjatiedot.oppijat.map(_.mahdollisetTekijäorganisaatiot.map(_.organisaatio)).map(_.toSet)
        .reduceLeft((a, b) => a.intersect(b))
        .toSeq

    pohjatiedot.copy(
      mahdollisetTekijäorganisaatiot = kaikissaOppijoissaEsiintyvätMahdollisetTekijäorganisaatiot
    )
  }
}
