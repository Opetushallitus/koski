package fi.oph.koski.todistus

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.schema._

import scala.xml.NodeSeq

class ValmaTodistusHtml(val koulutustoimija: Option[OrganisaatioWithOid], val oppilaitos: Oppilaitos, val oppijaHenkilö: Henkilötiedot, val todistus: Suoritus)(implicit val user: KoskiUser) extends ValmentavanKoulutuksenTodistusHtml {
  def title = "Ammatilliseen peruskoulutukseen valmentava koulutus"
  override def styles: NodeSeq = <link rel="stylesheet" type="text/css" href="/koski/css/todistus-ammatillinen-perustutkinto.css"></link>
}
