package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorModel, EditorModelSerializer}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.koskiuser.{KoskiSession, RequiresKansalainen}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.WithWarnings
import org.json4s.jackson.Serialization

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKansalainen with NoCache {

  get("/editor") {
    renderOmatTiedot
  }

  get("/editor/:oid") {
    val oid = params("oid")
    if (koskiSession.oid == oid) {
      renderOmatTiedot
    } else if (huollettavat.exists(_.oid == oid)) {
      renderHuollettava(oid)
    } else {
      haltWithStatus(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  private def renderHuollettava(huollettavanOid: String): Unit = {
    renderEither[EditorModel](toEditorModel(application.oppijaFacade.findUserOppija, application.oppijaFacade.findOppija(huollettavanOid)(KoskiSession.systemUser)))
  }

  private def renderOmatTiedot: Unit = {
    renderEither[EditorModel](toEditorModel(application.oppijaFacade.findUserOppija))
  }

  private def toEditorModel(oppijaE: Either[HttpStatus, WithWarnings[Oppija]]) =
    allowHuoltaja(oppijaE).map(OmatTiedotEditorModel.toEditorModel(_, None, huollettavat))

  private def toEditorModel(userOppijaE: Either[HttpStatus, WithWarnings[Oppija]], huollettavaE: Either[HttpStatus, WithWarnings[Oppija]]): Either[HttpStatus, EditorModel] = for {
    userOppija <- allowHuoltaja(userOppijaE)
    huollettava <- huollettavaE
  } yield OmatTiedotEditorModel.toEditorModel(userOppija, Some(huollettava), huollettavat)

  private def allowHuoltaja(oppija: Either[HttpStatus, WithWarnings[Oppija]]) = oppija.left.flatMap { status =>
    if (huollettavat.nonEmpty) {
      application.henkilöRepository.findByOid(koskiSession.oid)
        .map(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
        .map(henkilö => Right(WithWarnings(Oppija(henkilö, Nil), Nil)))
        .getOrElse(Left(status))
    } else {
      Left(status)
    }
  }

  private def huollettavat = {
    application.huollettavatService.getHuollettavatWithOid(koskiSession.oid).map(_.toHenkilötiedotJaOid)
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

