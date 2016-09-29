package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.scalaschema.annotation.Description

case class EsiopetuksenOpiskeluoikeus(
  id: Option[Int] = None,
  versionumero: Option[Int]  = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Oppilaitos,
  koulutustoimija: Option[Koulutustoimija] = None,
  alkamispäivä: Option[LocalDate] = None,
  arvioituPäättymispäivä: Option[LocalDate] = None,
  päättymispäivä: Option[LocalDate] = None,
  tila: OpiskeluoikeudenTila,
  läsnäolotiedot: Option[YleisetLäsnäolotiedot] = None,
  suoritukset: List[EsiopetuksenSuoritus],
  @KoodistoKoodiarvo("esiopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetus", koodistoUri = "opiskeluoikeudentyyppi")
) extends KoskeenTallennettavaOpiskeluoikeus {
  override def withIdAndVersion(id: Option[Int], versionumero: Option[Int]) = this.copy(id = id, versionumero = versionumero)
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def withSuoritukset(suoritukset: List[PäätasonSuoritus]) = copy(suoritukset = suoritukset.asInstanceOf[List[EsiopetuksenSuoritus]])
}

case class EsiopetuksenSuoritus(
  toimipiste: OrganisaatioWithOid,
  tila: Koodistokoodiviite,
  @KoodistoKoodiarvo("esiopetuksensuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("esiopetuksensuoritus", koodistoUri = "suorituksentyyppi"),
  vahvistus: Option[Vahvistus] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  koulutusmoduuli: Esiopetus = Esiopetus()
) extends PäätasonSuoritus with Toimipisteellinen {
  override def arviointi: Option[List[Arviointi]] = None
}

@Description("Esiopetuksen tunnistetiedot")
case class Esiopetus(
  @KoodistoKoodiarvo("001101")
  @KoodistoKoodiarvo("001102")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("001102", koodistoUri = "koulutus") // TODO: tarkista koodiarvo
) extends Koulutus {
  override def laajuus: Option[Laajuus] = None
}
