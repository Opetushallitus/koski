package fi.oph.koski.raportit

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio}

class RaportitService(application: KoskiApplication) {

  private lazy val raportointiDatabase = application.raportointiDatabase
  private lazy val perusopetusRepository = PerusopetuksenRaportitRepository(raportointiDatabase.db)

  def resolveRaportitOppilaitokselle(oppilaitosOid: Organisaatio.Oid): Set[String] = {
    val koulutusmuodot = raportointiDatabase.oppilaitoksenKoulutusmuodot(oppilaitosOid)

    koulutusmuodot.flatMap {
      case OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo => Seq("opiskelijavuositiedot", "suoritustietojentarkistus")
      case OpiskeluoikeudenTyyppi.perusopetus.koodiarvo => Seq("perusopetuksenvuosiluokka")
      case _ => Seq.empty
    }
  }

  def opiskelijaVuositiedot(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, Opiskelijavuositiedot)
  }

  def suoritustietojenTarkistus(request: AikajaksoRaporttiRequest): OppilaitosRaporttiResponse = {
    aikajaksoRaportti(request, SuoritustietojenTarkistus)
  }

  def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest): OppilaitosRaporttiResponse = {
    perusopetuksenVuosiluokka(request, PerusopetuksenVuosiluokka)
  }

  private def aikajaksoRaportti(request: AikajaksoRaporttiRequest, raporttiBuilder: AikajaksoRaportti) = {
    val rows = raporttiBuilder.buildRaportti(raportointiDatabase, request.oppilaitosOid, request.alku, request.loppu)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.alku, request.loppu, raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses).get))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      rows = rows,
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.alku, request.loppu), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.alku, request.loppu),
      downloadToken = request.downloadToken
    )
  }

  private def perusopetuksenVuosiluokka(request: PerusopetuksenVuosiluokkaRequest, raporttiBuilder: VuosiluokkaRaporttiPaivalta) = {
    val rows = raporttiBuilder.buildRaportti(perusopetusRepository, request.oppilaitosOid, request.paiva, request.vuosiluokka)
    val documentation = DocumentationSheet("Ohjeet", raporttiBuilder.documentation(request.oppilaitosOid, request.paiva, request.vuosiluokka, raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses).get))
    val data = DataSheet("Opiskeluoikeudet", rows, raporttiBuilder.columnSettings)

    OppilaitosRaporttiResponse(
      rows = rows,
      sheets = Seq(data, documentation),
      workbookSettings = WorkbookSettings(raporttiBuilder.title(request.oppilaitosOid, request.paiva, request.vuosiluokka), Some(request.password)),
      filename = raporttiBuilder.filename(request.oppilaitosOid, request.paiva, request.vuosiluokka),
      downloadToken = request.downloadToken
    )
  }
}
