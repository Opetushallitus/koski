package fi.oph.koski.schema

import fi.oph.koski.TestEnvironment
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, VirtaDerived, VirtaNote, VirtaSource}
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.EnumValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

// Korkeakoulu-skeeman jokainen kenttä kertoo Virta-lähteensä: puuttuva merkintä tarkoittaa lukijalle
// "ei Virrasta", joten kattavuus on pidettävä täydellisenä.
class VirtaSourceCoverageSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val korkeakouluClasses: Set[Class[_]] = Set(
    classOf[KorkeakoulunOpiskeluoikeus],
    classOf[KorkeakoulunOpiskeluoikeudenLisätiedot],
    classOf[SiirtoOpiskelija],
    classOf[LiittyväOpiskeluoikeus],
    classOf[KoulutuskuntaJakso],
    classOf[RahoituslähdeJakso],
    classOf[KorkeakoulunKoulutusala],
    classOf[KorkeakoulunLähdeorganisaatio],
    classOf[Liikkuvuusjakso],
    classOf[KorkeakoulunOpiskeluoikeudenLukuvuosimaksu],
    classOf[KorkeakoulututkinnonSuoritus],
    classOf[KorkeakoulunOpintojaksonSuoritus],
    classOf[MuuKorkeakoulunSuoritus],
    classOf[Korkeakoulututkinto],
    classOf[KorkeakoulunOpintojakso],
    classOf[MuuKorkeakoulunOpinto],
    classOf[KorkeakoulunOpiskeluoikeudenTila],
    classOf[KorkeakoulunOpiskeluoikeusjakso],
    classOf[KorkeakoulunKoodistostaLöytyväArviointi],
    classOf[KorkeakoulunPaikallinenArviointi],
    classOf[KorkeakoulunPaikallinenArvosana],
    classOf[Lukukausi_Ilmoittautuminen],
    classOf[Lukukausi_Ilmoittautumisjakso],
    classOf[Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu],
    classOf[Duplikaatti],
    classOf[OpiskeluoikeusAvaintaEiLöydy]
  )

  "Korkeakoulu-skeema" - {
    "jokaisella kentällä on täsmälleen yksi @VirtaSource tai @VirtaDerived" in {
      val rootSchema = KoskiSchema.createSchema(classOf[KorkeakoulunOpiskeluoikeus]).asInstanceOf[ClassSchema]
      val reached = allClassSchemas(rootSchema)(KoskiSchema.schemaFactory, rootSchema, collection.mutable.Set.empty)
      val inScope = reached.filter(s => korkeakouluClasses.map(_.getName).contains(s.fullClassName))

      val missingClasses = korkeakouluClasses.map(_.getName) -- inScope.map(_.fullClassName)
      missingClasses shouldBe empty

      // Synteettiset kentät (Opiskeluoikeus.alkamispäivä, Suoritus.tila, Arviointi.hyväksytty) periytyvät
      // yhteisistä traiteista ja lasketaan samoin kaikille koulutusmuodoille, joten ne eivät ole Virta-vastaavuuksia.
      // Yhteen arvoon sidottu kenttä (@KoodistoKoodiarvo, @EnumValue) ei tarvitse Virta-merkintää: skeema kertoo arvon.
      def vakio(p: Property): Boolean = p.metadata.exists(m => m.isInstanceOf[KoodistoKoodiarvo] || m.isInstanceOf[EnumValue])
      val violations = inScope.flatMap { s =>
        s.properties.filterNot(p => p.synthetic || vakio(p)).collect {
          case p if p.metadata.count(m => m.isInstanceOf[VirtaSource] || m.isInstanceOf[VirtaDerived]) != 1 =>
            s.simpleName + "." + p.key
          case p if p.metadata.count(_.isInstanceOf[VirtaNote]) > 1 =>
            s.simpleName + "." + p.key + " (useita @VirtaNote)"
        }
      }
      withClue(violations.mkString("Puuttuva tai moninkertainen Virta-merkintä:\n", "\n", "\n")) {
        violations shouldBe empty
      }
    }
  }

  private def allClassSchemas(schema: Schema)(implicit factory: SchemaFactory, rootSchema: Schema, covered: collection.mutable.Set[String]): List[ClassSchema] = schema match {
    case s: ClassRefSchema => allClassSchemas(s.resolve(factory, rootSchema))
    case s: SchemaWithClassName if covered.contains(s.fullClassName) => Nil
    case s: ClassSchema =>
      covered.add(s.fullClassName)
      s :: s.properties.map(_.schema).flatMap(allClassSchemas)
    case s: AnyOfSchema =>
      covered.add(s.fullClassName)
      s.alternatives.flatMap(allClassSchemas)
    case s: OptionalSchema => allClassSchemas(s.itemSchema)
    case s: ListSchema => allClassSchemas(s.itemSchema)
    case s: MapSchema => allClassSchemas(s.itemSchema)
    case _ => Nil
  }
}
