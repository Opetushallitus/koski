package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi
import fi.oph.koski.tutkinto.TutkintoRepository

class EPerusteetValidationUtils(
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) {

  def haeKoulutustyyppi(diaarinumero: String): Option[Koulutustyyppi] =
  // Lue koulutustyyppi aina uusimmasta perusteesta. Käytännössä samalla diaarinumerolla
  // julkaistuissa perusteessa koulutustyyppi ei voi vaihtua.
    tutkintoRepository.findUusinPerusteRakenne(diaarinumero).map(r => r.koulutustyyppi)

  def onKoodistossa(diaarinumero: String): Boolean =
    koodistoViitePalvelu.onKoodistossa("koskikoulutustendiaarinumerot", diaarinumero)
}
