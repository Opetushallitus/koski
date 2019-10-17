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
    renderEither[EditorModel](toEditorModel(application.oppijaFacade.findUserOppija))
  }

  get("/editor/:oid") {
    val oid = params("oid")
    if (hasAccess(oid)) {
      val model = toEditorModel(application.oppijaFacade.findUserOppija, application.oppijaFacade.findOppija(oid)(KoskiSession.systemUser))
      renderEither[EditorModel](model)
    } else {
      haltWithStatus(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  private def toEditorModel(oppijaE: Either[HttpStatus, WithWarnings[Oppija]]) = oppijaE.map(OmatTiedotEditorModel.toEditorModel(_, None))

  private def toEditorModel(userOppijaE: Either[HttpStatus, WithWarnings[Oppija]], huollettavaE: Either[HttpStatus, WithWarnings[Oppija]]): Either[HttpStatus, EditorModel] = for {
    userOppija <- userOppijaE
    huollettava <- huollettavaE
  } yield OmatTiedotEditorModel.toEditorModel(userOppija, Some(huollettava))

  private def hasAccess(oid: String) = {
    koskiSession.oid == oid || huollettavat.contains(oid)
  }

  private def huollettavat = {
    application.huollettavatService.getHuollettavatWithOid(koskiSession.oid).map(_.oid)
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

