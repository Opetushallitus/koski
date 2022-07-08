package fi.oph.koski.schema

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{OksaUri, SensitiveData, Tooltip}
import fi.oph.scalaschema.annotation.{DefaultValue, Description, OnlyWhen, Title}

trait TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot extends OpiskeluoikeudenLisätiedot with MaksuttomuusTieto

@Title("Tutkintokoulutukseen valmentavan opiskeluoikeuden ammatillisen koulutuksen järjestämisluvan lisätiedot")
@Description("Tutkintokoulutukseen valmentavan opiskeluoikeuden ammatillisen koulutuksen järjestämisluvan lisätiedot")
@OnlyWhen("../järjestämislupa/koodiarvo", "ammatillinen")
case class TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None,
  @Description("Koulutuksen tarjoajan majoitus, huoneeseen muuttopäivä ja lähtöpäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Koulutuksen järjestäjän tarjoama(t) majoitusjakso(t). Huoneeseen muuttopäivä ja lähtöpäivä. Rahoituksen laskennassa käytettävä tieto.")
  majoitus: Option[List[Aikajakso]] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  @Description("Vaativan erityisen tuen yhteydessä järjestettävä majoitus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Vaativan erityisen tuen yhteydessä järjestettävä majoitus (aloitus- ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[Aikajakso]] = None,
  @Description("Tieto siitä että oppija on erityisopetuksessa, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on erityisopetuksessa. Merkitään erityisopetuksen alku- ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityinenTuki: Option[List[Aikajakso]] = None,
  @Description("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Lista alku-loppu päivämääräpareja. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä että oppija on vaativan erityisen tuen erityisen tehtävän erityisen tuen piirissä (aloituspäivä ja loppupäivä). Voi olla useita erillisiä jaksoja. Oppilaitoksen opetusluvassa tulee olla myönnetty vaativan erityisen tuen tehtävä. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenErityinenTehtävä: Option[List[Aikajakso]] = None,
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Osallistuuko oppija vaikeasti vammaisille järjestettyyn opetukseen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, osallistuuko oppija vaikeasti vammaisille järjestettyyn opetukseen (alku- ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @Title("Vaikeasti vammaisille järjestetty opetus")
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vammainen ja hänellä on avustaja. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Onko oppija vammainen ja hänellä on avustaja (alku- ja loppupäivä). Voit olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  vammainenJaAvustaja: Option[List[Aikajakso]] = None,
  @Description("Kyseessä on osa-aikainen opiskelu. Lista alku-loppu päivämääräpareja. Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole osa-aikainen opiskelu. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Osa-aikaisuusjaksojen tiedot (jakson alku- ja loppupäivät sekä osa-aikaisuus prosenttilukuna). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @Title("Osa-aikaisuusjaksot")
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None,
  @Description("Kyseessä on vankilaopetus. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto")
  @Tooltip("Tieto vankilaopetusjaksoista (alku- ja loppupäivämäärä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vankilaopetuksessa: Option[List[Aikajakso]] = None,
  @Description("Onko kyseessä koulutusvientikoulutus (kyllä/ei). Kentän välittämättä jättäminen tulkitaan että kyseessä ei ole koulutusvientikoulutus.")
  @Tooltip("Valitse valintaruutu, jos kyseessä on koulutusvientikoulutus.")
  @DefaultValue(false)
  koulutusvienti: Boolean = false,
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false).")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
) extends TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  with Ulkomaajaksollinen
  with SisäoppilaitosmainenMajoitus
  with VaikeastiVammainen

@Title("Tutkintokoulutukseen valmentavan opiskeluoikeuden lukiokoulutuksen järjestämisluvan lisätiedot")
@Description("Tutkintokoulutukseen valmentavan opiskeluoikeuden lukiokoulutuksen järjestämisluvan lisätiedot")
@OnlyWhen("../järjestämislupa/koodiarvo", "lukio")
case class TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None,
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false).")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  @Description("Onko opiskelija sisöoppilaitosmaisessa majoituksessa. Rahoituksen laskennassa käytettävä tieto.")
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
) extends TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot

@Title("Tutkintokoulutukseen valmentavan opiskeluoikeuden perusopetuksen järjestämisluvan lisätiedot")
@Description("Tutkintokoulutukseen valmentavan opiskeluoikeuden perusopetuksen järjestämisluvan lisätiedot")
@OnlyWhen("../järjestämislupa/koodiarvo", "perusopetus")
case class TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
  maksuttomuus: Option[List[Maksuttomuus]] = None,
  oikeuttaMaksuttomuuteenPidennetty: Option[List[OikeuttaMaksuttomuuteenPidennetty]] = None,
  @Description("Rahoituksen laskennassa käytettävä tieto.")
  ulkomaanjaksot: Option[List[Ulkomaanjakso]] = None,
  @Description("Onko oppija muu kuin vaikeimmin kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija muu kuin vaikeimmin kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  vammainen: Option[List[Aikajakso]] = None,
  @Description("Onko oppija vaikeasti kehitysvammainen. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, onko oppija vaikeasti kehitysvammainen (alku- ja loppupäivämäärät). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  vaikeastiVammainen: Option[List[Aikajakso]] = None,
  @Description("Oppilaalla on majoitusetu (alku- ja loppupäivämäärä). Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos oppilaalla on majoitusetu (alku- ja loppupäivämäärät). Rahoituksen laskennassa käytettävä tieto.")
  majoitusetu: Option[Aikajakso] = None,
  @Description("Oppilaalla on kuljetusetu (alku- ja loppupäivämäärät).")
  @Tooltip("Tieto siitä, jos oppilaalla on kuljetusetu (alku- ja loppupäivämäärät).")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  kuljetusetu: Option[Aikajakso] = None,
  @Description("Sisäoppilaitosmuotoinen majoitus, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen sisäoppilaitosmuotoisen majoituksen aloituspäivä ja loppupäivä. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  sisäoppilaitosmainenMajoitus: Option[List[Aikajakso]] = None,
  @Description("Oppija on koulukotikorotuksen piirissä, aloituspäivä ja loppupäivä. Lista alku-loppu päivämääräpareja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Tieto siitä, jos oppija on koulukotikorotuksen piirissä (aloituspäivä ja loppupäivä). Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  koulukoti: Option[List[Aikajakso]] = None,
  @Description("Pidennetty oppivelvollisuus alkamis- ja päättymispäivineen. Kentän puuttuminen tai null-arvo tulkitaan siten, että oppilaalla ei ole pidennettyä oppivelvollisuutta. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen pidennetyn oppivelvollisuuden alkamis- ja päättymispäivät. Rahoituksen laskennassa käytettävä tieto.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  @OksaUri("tmpOKSAID517", "pidennetty oppivelvollisuus")
  pidennettyOppivelvollisuus: Option[Aikajakso] = None,
  @Description("Opiskeluajan pidennetty päättymispäivä (true/false).")
  @DefaultValue(false)
  pidennettyPäättymispäivä: Boolean = false,
  @Description("Erityisen tuen päätökset alkamis- ja päättymispäivineen. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @Tooltip("Mahdollisen erityisen tuen päätösten alkamis- ja päättymispäivät. Voi olla useita erillisiä jaksoja. Rahoituksen laskennassa käytettävä tieto.")
  @OksaUri("tmpOKSAID281", "henkilökohtainen opetuksen järjestämistä koskeva suunnitelma")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]] = None
) extends TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
