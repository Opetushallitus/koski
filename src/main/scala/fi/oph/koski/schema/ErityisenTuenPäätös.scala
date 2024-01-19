package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{Deprecated, KoodistoUri, OksaUri, RedundantData, SensitiveData, Tooltip}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

@Description("Oppivelvollisen erityisen tuen päätöstiedot")
case class ErityisenTuenPäätös(
  @Description("Jakson alkamispäivämäärä. Muoto YYYY-MM-DD")
  alku: Option[LocalDate],
  @Description("Jakson loppumispäivämäärä. Muoto YYYY-MM-DD")
  loppu: Option[LocalDate],
  @Description(
    """Oppilas opiskelee toiminta-alueittain (true/false).
Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain.
Tuolloin oppilaalla on aina erityisen tuen päätös.
Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.
Huom: toiminta-alue arviointeineen on kuvattu oppiaineen suorituksessa.""")
  @Tooltip("Opiskeleeko oppilas toiminta-alueittain? Toiminta-alueittain opiskelussa oppilaalla on yksilöllistetty oppimäärä ja opetus järjestetty toiminta-alueittain. Tuolloin oppilaalla on aina erityisen tuen päätös. Oppilaan opetussuunnitelmaan kuuluvat toiminta-alueet ovat motoriset taidot, kieli ja kommunikaatio, sosiaaliset taidot, päivittäisten toimintojen taidot ja kognitiiviset taidot.")
  @Title("Opiskelee toiminta-alueittain")
  opiskeleeToimintaAlueittain: Boolean = false,
  @Deprecated("Ei käytössä 19.1.2024 eteenpäin")
  @Description("Tätä tietoa ei tarvitse jatkossa välittää. Tieto erityisryhmän toteutuspaikasta välitetään toteutuspaikka-rakenteessa.")
  @Tooltip("Suorittaako erityisoppilas koulutusta omassa erityisryhmässään vai inklusiivisesti opetuksen mukana.")
  @OksaUri("tmpOKSAID444", "opetusryhmä")
  @Title("Opiskelee erityisryhmässä")
  erityisryhmässä: Option[Boolean],
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
