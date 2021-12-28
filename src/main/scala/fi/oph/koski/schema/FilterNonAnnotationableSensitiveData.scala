package fi.oph.koski.schema

import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli

// Tietynlaisen datan filtteröinti pois alkuperäisellä annotaatiopohjaisella tavalla (@SensitiveData-tägäys) osoittautui
// riittämättömäksi ratkaisemaan tiketin 1327 vaatima lisätietolistan filtteröinti.
// Päädyttiin toteuttamaan filtteröinti kun opiskeluoikeus on serialisoitu tietokannasta Scala-luokista muodostettuihin
// objekteihin.
// Tällöin on myös voitu välttää uuden mahdollisesti raskaan reflektion käyttö serialisoitaessa Scala-objekteja
// JSON-dataksi.

object FilterNonAnnotationableSensitiveData {
  def filter(oo: KoskeenTallennettavaOpiskeluoikeus)(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case oo: AmmatillinenOpiskeluoikeus =>
        filterAmmatillinen(oo)
      case _ => oo
    }
  }

  private def filterAmmatillinen(oo: AmmatillinenOpiskeluoikeus)(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus = {
    oo.withSuoritukset(
      oo.suoritukset.map(suoritus =>
        if (suoritus.osasuoritukset.nonEmpty) {
          suoritus.withOsasuoritukset(
            Some(filterAmmatillinenOsasuoritukset(suoritus.osasuoritusLista))
          )
        }
        else {
          suoritus
        }
    ))
  }

  private def filterAmmatillinenOsasuoritukset(osasuoritukset: List[Suoritus])(implicit user: SensitiveDataAllowed): List[Suoritus] = {
    osasuoritukset.map {
      case lisätiedollinen: AmmatillisenTutkinnonOsanLisätiedollinen =>
        val lisätiedot = if(lisätiedollinen.lisätiedot.nonEmpty) {
          Some(lisätiedollinen.lisätiedot.toList.flatten.filter(
            _.tunniste.koodiarvo != "mukautettu" ||
              user.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA, Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU))
          ))
        } else {
          None
        }
       val alaosasusoritukset = if (lisätiedollinen.osasuoritusLista.nonEmpty) {
          Some(filterAmmatillinenOsasuoritukset(lisätiedollinen.osasuoritusLista))
        } else  {
          None
        }
        lisätiedollinen.withLisätiedot(lisätiedot).withOsasuoritukset(alaosasusoritukset)
      case osasuoritus => osasuoritus
    }
  }
}
