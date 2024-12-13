package fi.oph.koski.suoritusjako

import fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeus
import fi.oph.koski.schema
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Oppija}
import fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotOpiskeluoikeus
import fi.oph.scalaschema.{ClassSchema, SchemaToJson}
import org.json4s.JValue

import java.time.LocalDate

case class OppijaJakolinkillä(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: Henkilö,
  opiskeluoikeudet: Seq[Opiskeluoikeus]
) {
  def toOppija(): Oppija =
    Oppija(henkilö, opiskeluoikeudet)
}

object SuoritetutTutkinnotOppijaJakolinkillä {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[SuoritetutTutkinnotOppijaJakolinkillä]).asInstanceOf[ClassSchema])
}

case class SuoritetutTutkinnotOppijaJakolinkillä(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: SuoritusjakoHenkilö,
  opiskeluoikeudet: List[SuoritetutTutkinnotOpiskeluoikeus]
)

object AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä {
  lazy val schemaJson: JValue =
    SchemaToJson.toJsonSchema(schema.KoskiSchema.createSchema(classOf[AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä]).asInstanceOf[ClassSchema])
}

case class AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä(
  jakolinkki: Option[Jakolinkki] = None,
  henkilö: SuoritusjakoHenkilö,
  opiskeluoikeudet: List[AktiivisetJaPäättyneetOpinnotOpiskeluoikeus]
)

case class Jakolinkki(
  voimassaAsti: LocalDate
)

case class SuoritusjakoHenkilö(
  oid: String,
  syntymäaika: Option[LocalDate],
  etunimet: String,
  sukunimi: String,
  kutsumanimi: String
)
object SuoritusjakoHenkilö {
  def fromOppijaHenkilö(oppijaHenkilö: fi.oph.koski.henkilo.OppijaHenkilö) = SuoritusjakoHenkilö(
    oid = oppijaHenkilö.oid,
    syntymäaika = oppijaHenkilö.syntymäaika,
    etunimet = oppijaHenkilö.etunimet,
    sukunimi = oppijaHenkilö.sukunimi,
    kutsumanimi = oppijaHenkilö.kutsumanimi
  )
}

