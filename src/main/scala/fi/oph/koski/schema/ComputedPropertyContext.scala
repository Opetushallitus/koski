package fi.oph.koski.schema

import fi.oph.koski.organisaatio.OrganisaatioRepository

import scala.util.DynamicVariable

// Käytössä vain synkronisen serialisoinnin aikana laskettavien kenttien kontekstina.
class ComputedPropertyContext(
  private val organisaatioRepository: OrganisaatioRepository
) {
  def oppilaitostyyppi(oid: Organisaatio.Oid): Option[Koodistokoodiviite] =
    organisaatioRepository
      .getOrganisaatioHierarkia(oid)
      .flatMap(_.oppilaitostyyppi)
      .map(resolveOppilaitostyyppi)

  private def resolveOppilaitostyyppi(koodiarvo: String): Koodistokoodiviite =
    organisaatioRepository.koodisto
      .validate("oppilaitostyyppi", koodiarvo)
      .getOrElse(Koodistokoodiviite(koodiarvo, "oppilaitostyyppi"))
}

object ComputedPropertyContext {
  private val currentContext = new DynamicVariable[Option[ComputedPropertyContext]](None)

  private def compute[A](f: ComputedPropertyContext => Option[A]): Option[A] =
    currentContext.value.flatMap(f)

  def oppilaitostyyppi(oid: Organisaatio.Oid): Option[Koodistokoodiviite] =
    compute(_.oppilaitostyyppi(oid))

  def withOptionalContext[T](context: Option[ComputedPropertyContext])(block: => T): T =
    context.fold(block)(ctx => currentContext.withValue(Some(ctx))(block))
}
