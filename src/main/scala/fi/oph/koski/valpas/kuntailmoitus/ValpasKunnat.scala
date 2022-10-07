package fi.oph.koski.valpas.kuntailmoitus

import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.AhvenanmaanKunnat
import fi.oph.koski.schema.OrganisaatioWithOid
import fi.oph.koski.valpas.oppija.ValpasAccessResolver
import fi.oph.koski.valpas.valpasuser.ValpasSession

object ValpasKunnat {
  private val accessResolver = new ValpasAccessResolver

  def getKunnat(organisaatioService: OrganisaatioService): Seq[OrganisaatioWithOid] =
    organisaatioService
      .aktiivisetKunnat()
      .filterNot(AhvenanmaanKunnat.onAhvenanmaalainenKunta)

  def getUserKunnat(organisaatioService: OrganisaatioService)(implicit session: ValpasSession): Seq[OrganisaatioWithOid] =
    getKunnat(organisaatioService)
      .filter(kunta => kunta.kotipaikka.exists(koodiviite => accessResolver.accessToKuntaOrg(koodiviite.koodiarvo)))
}
