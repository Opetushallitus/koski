package fi.oph.koski.kela

import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.reflect.runtime.universe._

class KelaTutkintokoulutukseenValmentavanKoulutuksenSchemaSpec
  extends AnyFreeSpec
    with Matchers {

  "Tietomallin Kela-skeema on tarkistettu tarvittavien lisäysten varalta" in {
    fieldDifferences[KelaTuvaOpiskeluoikeudenLisätiedot, TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot] should equal(
      List("pidennettyOppivelvollisuus", "kuljetusetu", "vaikeastiVammainen", "vammainen", "pidennettyPäättymispäivä"))
  }

  private def fieldDifferences[T1: TypeTag, T2: TypeTag]: List[String] = {
    val t1s = classAccessors[T1].map(_.name.toString).sorted.toSet
    val t2s = classAccessors[T2].map(_.name.toString).sorted.toSet
    t2s.diff(t1s).toList
  }

  private def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList


}
