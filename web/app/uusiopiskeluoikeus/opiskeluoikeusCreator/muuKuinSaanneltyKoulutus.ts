import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuKuinSäänneltyKoulutus } from '../../types/fi/oph/koski/schema/MuuKuinSaanneltyKoulutus'
import { MuunKuinSäännellynKoulutuksenLisätiedot } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenLisatiedot'
import { MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeudenJakso'
import { MuunKuinSäännellynKoulutuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenOpiskeluoikeus'
import { MuunKuinSäännellynKoulutuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import { MuunKuinSäännellynKoulutuksenTila } from '../../types/fi/oph/koski/schema/MuunKuinSaannellynKoulutuksenTila'
import { toOppilaitos, toToimipiste } from './utils'

// Muu kuin säännelty koulutus
export const createMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso['tila'],
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  suorituskieli: Koodistokoodiviite<'kieli'>,
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet'>,
  jotpaAsianumero: Koodistokoodiviite<'jotpaasianumero'>
) =>
  MuunKuinSäännellynKoulutuksenOpiskeluoikeus({
    oppilaitos: toOppilaitos(organisaatio),
    tila: MuunKuinSäännellynKoulutuksenTila({
      opiskeluoikeusjaksot: [
        MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso({
          alku,
          tila,
          opintojenRahoitus
        })
      ]
    }),
    lisätiedot: MuunKuinSäännellynKoulutuksenLisätiedot({
      jotpaAsianumero
    }),
    suoritukset: [
      MuunKuinSäännellynKoulutuksenPäätasonSuoritus({
        suorituskieli,
        koulutusmoduuli: MuuKuinSäänneltyKoulutus({
          opintokokonaisuus: opintokokonaisuus
        }),
        toimipiste: toToimipiste(organisaatio)
      })
    ]
  })
