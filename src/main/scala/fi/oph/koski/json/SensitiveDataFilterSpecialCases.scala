package fi.oph.koski.json

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus, AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus, AmmatillisenTutkinnonOsanLisätiedollinen, KorkeakouluopintojenSuoritus, KoskeenTallennettavaOpiskeluoikeus, OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus, OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus, Suoritus}

object SensitiveDataFilterSpecialCases {
  def filterSpecialCases(oo: KoskeenTallennettavaOpiskeluoikeus)(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus = {
    oo match {
      case oo: AmmatillinenOpiskeluoikeus =>
        filterAmmatillinen(oo)
      case _ => oo
    }
  }

  def filterAmmatillinen(oo: AmmatillinenOpiskeluoikeus)(implicit user: SensitiveDataAllowed): KoskeenTallennettavaOpiskeluoikeus = {
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

  def filterAmmatillinenOsasuoritukset(osasuoritukset: List[Suoritus])(implicit user: SensitiveDataAllowed): List[Suoritus] = {
    osasuoritukset.map {
      case lisätiedollinen: AmmatillisenTutkinnonOsanLisätiedollinen if !lisätiedollinen.isInstanceOf[OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus]
        && !lisätiedollinen.isInstanceOf[OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus]
        && !lisätiedollinen.isInstanceOf[AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus]
        && !lisätiedollinen.isInstanceOf[AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus] =>
        val lisätiedot = if(lisätiedollinen.lisätiedot.nonEmpty) {
          Some(lisätiedollinen.lisätiedot.toList.flatten.filter(
            _.tunniste.koodiarvo == "mukautettu" &&
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
