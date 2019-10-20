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

  private def renderOmatTiedot: Unit = {
    renderEither[EditorModel](toEditorModel(findUserOppijaAllowEmpty))
  }

  private def renderHuollettava(huollettavanOid: String): Unit = {
    renderEither[EditorModel](toEditorModel(findUserOppijaAllowEmpty, findHuollettavaOppija(huollettavanOid)))
  }

  private def findUserOppijaAllowEmpty =
    allowEmpty(application.oppijaFacade.findUserOppija, koskiSession.oid)

  private def findHuollettavaOppija(huollettavanOid: String) =
    allowEmpty(application.oppijaFacade.findOppija(huollettavanOid)(KoskiSession.systemUser), huollettavanOid)

  private def toEditorModel(oppijaE: Either[HttpStatus, WithWarnings[Oppija]]) =
    oppijaE.map(OmatTiedotEditorModel.toEditorModel(_, None, huollettavat))

  private def toEditorModel(userOppijaE: Either[HttpStatus, WithWarnings[Oppija]], huollettavaE: Either[HttpStatus, WithWarnings[Oppija]]): Either[HttpStatus, EditorModel] = for {
    userOppija <- userOppijaE
    huollettava <- huollettavaE
  } yield OmatTiedotEditorModel.toEditorModel(userOppija, Some(huollettava), huollettavat)

  private def allowEmpty(oppija: Either[HttpStatus, WithWarnings[Oppija]], oid: String) = if (huollettavat.nonEmpty) {
    oppija.left.flatMap { status =>
      application.henkilöRepository.findByOid(oid)
        .map(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
        .map(henkilö => Right(WithWarnings(Oppija(henkilö, Nil), Nil)))
        .getOrElse(Left(status))
    }
  } else {
    oppija
  }

  private def huollettavat = {
    application.huollettavatService.getHuollettavatWithOid(koskiSession.oid).map(_.toHenkilötiedotJaOid)
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

