package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, OksaUri, RedundantData, SensitiveData, Tooltip}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

@Description("Oppivelvollisen tukea koskevat päätöstiedot")
case class TukeaKoskevaPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description("Kenttä käytössä 1.8.2026 alkaen.")
  @Description(
    """Oppilas opiskelee toiminta-alueittain (true/false).
Toiminta-alueittain opiskelussa oppilaalla on rajattu oppimäärä ja opetus järjestetty toiminta-alueittain.
Tuolloin oppilaalla on aina tukea koskeva päätös.
Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
Huom: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  @Tooltip("Opiskeleeko oppilas toiminta-alueittain? Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain. Tuolloin oppilaalla on aina tukea koskeva päätös. Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.")
  @Title("Opiskelee toiminta-alueittain")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Description("Tieto erityisopetuksen toteutuspaikasta välitetään tämän rakenteen koodiston mukaisesti.")
  @Tooltip("Tieto erityisopetuksen toteutuspaikasta.")
  @KoodistoUri("erityisopetuksentoteutuspaikka")
  @Title("Erityisopetuksen toteutuspaikka")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @RedundantData
  toteutuspaikka: Option[Koodistokoodiviite] = None,
  @RedundantData
  tukimuodot: Option[List[Koodistokoodiviite]] = None
) extends MahdollisestiAlkupäivällinenJakso with Tukimuodollinen
