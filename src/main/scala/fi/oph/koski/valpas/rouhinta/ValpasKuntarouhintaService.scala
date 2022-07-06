package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.OppijaNumerorekisteriKuntarouhintaOppija
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö.Oid
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.util.Futures
import fi.oph.koski.valpas.ValpasErrorCategory
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö

import scala.concurrent.Future
import scala.util.control.NonFatal

class ValpasKuntarouhintaService(application: KoskiApplication)
  extends ValpasRouhintaTiming
    with DatabaseConverters
    with Logging
    with GlobalExecutionContext
{
  private val oppijalistatService = application.valpasOppijalistatService
  private val rouhintaOvKeskeytyksetService = application.valpasRouhintaOppivelvollisuudenKeskeytysService

  def haeKunnanPerusteellaIlmanOikeustarkastusta
    (kunta: String)
  : Either[HttpStatus, KuntarouhinnanTulos] = {
    // Hae oppijanumerorekisterin tiedot rinnakkain Kosken tietojen kanssa. Kosken tiedot pitää hakea, koska ONR:n
    // sivutuksen vuoksi yksittäisiä Kosken tuntemia oppijoita voi jäädä puuttumaan sen palauttamalta listalta.
    val eiOppivelvollisuuttaSuorittavatOnrissaFuture: Future[Either[HttpStatus, Seq[ValpasRouhintaOppivelvollinen]]] =
      Future {
        try {
          Right(rouhiOppivelvollisuuttaSuorittamattomatOppijanumerorekisteristä(kunta))
        } catch {
          case NonFatal(e) => {
            val msg = "Tietoja ei saatu haettua oppijanumerorekisteristä"
            logger.error(e)(msg)
            Left(ValpasErrorCategory.internalError(msg))
          }
        }
      }

    val eiOppivelvollisuuttaSuorittavatKoskessa = rouhiOppivelvollisuuttaSuorittamattomatKoskesta(kunta)
    val eiOppivelvollisuuttaSuorittavatOnrissa = Futures.await(eiOppivelvollisuuttaSuorittavatOnrissaFuture)

    HttpStatus.foldEithers(Seq(
      eiOppivelvollisuuttaSuorittavatKoskessa,
      eiOppivelvollisuuttaSuorittavatOnrissa
    ))
      .map(_.flatten)
      .map(oppijat => KuntarouhinnanTulos(
        eiOppivelvollisuuttaSuorittavat = oppijat
      ))
  }

  private def rouhiOppivelvollisuuttaSuorittamattomatOppijanumerorekisteristä
    (kunta: String)
  : Seq[ValpasRouhintaOppivelvollinen] = {
    timed("rouhiOppivelvollisuuttaSuorittamattomatOppijanumerorekisteristä") {
      // Asetetaan maksimi käsiteltävän datan määrälle, jotta esim. bugit eivät aiheuta ikikiersiötä.
      // ONR API palauttaa tällä hetkellä oppijat 5 000 oppijan sivuina. Käytännössä ikäluokka ei suurimmassa kunnassa
      // Helsingissä ole kuin max 90 000. QA:lla kuitenkin on testidatassa pelkästään helsinkiläisiä ja tällä hetkellä
      // testikäytössä olevien laajennettujen oppivelvollisuuden ikärajojen vuoksi heitä on lähes 200 000.
      val viimeinenSivu = 50
      val ylivuotosivu = viimeinenSivu + 1

      (1 to ylivuotosivu)
        // Käytä iteraattoria, jotta oppijanumerorekisterin pommitukseen tulee pieni tauko, kun poistetaan Kosken tuntemat
        // oppijat kutsujen välissä:
        .toIterator
        .map(tarkistaYlivuoto(ylivuotosivu))
        .map(haeSivuOppijoitaOppijanumerorekisteristä(kunta))
        .takeWhile(!_.isEmpty)
        .flatMap(poistaKoskenTuntematOppijat)
        .filter(onHetullinenOppija)
        .map(ValpasRouhintaOppivelvollinen.apply)
        .filter(o => Oppivelvollisuustiedot.onOppivelvollinenPelkänIänPerusteella(o.syntymäaika, application.valpasRajapäivätService))
        // Pakota iteraattorin evaluointi rinnakkaistuksen helpottamiseksi ja ajastuslogitusten vuoksi
        .toList
    }
  }

  private def tarkistaYlivuoto(maxPages: Int)(page: Int): Int = {
    if (page == maxPages) {
      throw new InternalError("Oppijanumerorekisterin sivutuksessa ongelmia tai datamäärä kasvanut yli rajojen. Kaikkia oppijoita ei välttämättä käsitelty.")
    }
    page
  }

  private def haeSivuOppijoitaOppijanumerorekisteristä
    (kunta: String)
    (page: Int): Seq[OppijaNumerorekisteriKuntarouhintaOppija] =
  {
    timed("haeSivuOppijoitaOppijanumerorekisteristä") {
      application.opintopolkuHenkilöFacade
        .findByVarhaisinSyntymäaikaAndKotikunta(varhaisinSyntymäaika.toString, kunta, page)
        .results
    }
  }

  private lazy val varhaisinSyntymäaika =
    application.valpasRajapäivätService.aikaisinMahdollinenOppivelvollisenSyntymäaika

  private def poistaKoskenTuntematOppijat
    (oppijat: Seq[OppijaNumerorekisteriKuntarouhintaOppija])
  : Seq[OppijaNumerorekisteriKuntarouhintaOppija] =
  {
    timed("poistaKoskenTuntematOppijat") {
      val oids = oppijat.map(_.oidHenkilo)
      val koskessa = application.valpasOpiskeluoikeusDatabaseService.haeTunnettujenOppijoidenOidit(oids).map(_.oppijaOid).toSet
      oppijat.filter(o => !koskessa.contains(o.oidHenkilo))
    }
  }

  private def onHetullinenOppija(oppija: OppijaNumerorekisteriKuntarouhintaOppija): Boolean =
    oppija.hetu.isDefined

  private def rouhiOppivelvollisuuttaSuorittamattomatKoskesta
    (kunta: String)
  : Either[HttpStatus, Seq[ValpasRouhintaOppivelvollinen]] = {
    val oppivelvollisetKoskessa = getOppivelvollisetKotikunnalla(kunta)

    rouhintaTimed("haeKunnanPerusteellaKoskesta", oppivelvollisetKoskessa.size) {
      oppijalistatService
        // Kunnan käyttäjällä on aina oikeudet kaikkiin oppijoihin, joilla on oppivelvollisuus voimassa, joten
        // käyttöoikeustarkistusta ei tarvitse tehdä
        .getOppijalistaIlmanOikeustarkastusta(oppivelvollisetKoskessa)
        .flatMap(oppivelvollisetKoskessa => {
          rouhintaTimed("haeKunnanPerusteella:KuntarouhinnanTulos", oppivelvollisetKoskessa.size) {
            val eiSuorittavat =
              oppivelvollisetKoskessa
                .filterNot(_.oppija.suorittaaOppivelvollisuutta)

            oppijalistatService.withKuntailmoituksetIlmanKäyttöoikeustarkistusta(eiSuorittavat)
              .map(_.map(ValpasRouhintaOppivelvollinen.apply))
              .map(oppijat => rouhintaOvKeskeytyksetService.fetchOppivelvollisuudenKeskeytykset(oppijat))
          }
        })
    }
  }

  private def getOppivelvollisetKotikunnalla(kunta: String): Seq[ValpasHenkilö.Oid] = {
    timed("getOppivelvollisetKotikunnalla") {
      oppijalistatService.getOppivelvollisetKotikunnallaIlmanOikeustarkastusta(kunta).map(_.masterOid)
    }
  }
}

case class KuntarouhinnanTulos(
  eiOppivelvollisuuttaSuorittavat: Seq[ValpasRouhintaOppivelvollinen],
) {
  def palautetutOppijaOidit: Seq[Oid] = eiOppivelvollisuuttaSuorittavat.map(_.oppijanumero)
}
