import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { LukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { LukioonValmistavaKoulutus } from '../../types/fi/oph/koski/schema/LukioonValmistavaKoulutus'
import { LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import { LukioonValmistavanKoulutuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenOpiskeluoikeus'
import { LukioonValmistavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenSuoritus'
import { maksuttomuuslisätiedot, toOppilaitos, toToimipiste } from './utils'

export const createLukioonValmistavanKoulutuksenOpiskeluoikeus = (
  peruste: Peruste,
  organisaatio: OrganisaatioHierarkia,
  alku: string,
  tila: LukionOpiskeluoikeusjakso['tila'],
  suorituskieli: Koodistokoodiviite<'kieli'>,
  opintojenRahoitus: Koodistokoodiviite<'opintojenrahoitus', any>,
  maksuton: boolean | null
) => {
  const oppimäärä = perusteToOppimäärä(peruste)

  return (
    oppimäärä &&
    LukioonValmistavanKoulutuksenOpiskeluoikeus({
      oppilaitos: toOppilaitos(organisaatio),
      tila: LukionOpiskeluoikeudenTila({
        opiskeluoikeusjaksot: [
          LukionOpiskeluoikeusjakso({
            alku,
            tila,
            opintojenRahoitus
          })
        ]
      }),
      suoritukset: [
        LukioonValmistavanKoulutuksenSuoritus({
          koulutusmoduuli: LukioonValmistavaKoulutus({
            perusteenDiaarinumero: peruste.koodiarvo
          }),
          suorituskieli,
          oppimäärä,
          toimipiste: toToimipiste(organisaatio)
        })
      ],
      lisätiedot: maksuttomuuslisätiedot(
        alku,
        maksuton,
        LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
      )
    })
  )
}

const perusteToOppimäärä = (
  peruste: Peruste
): Koodistokoodiviite<'lukionoppimaara'> | undefined => {
  switch (peruste.koodiarvo) {
    case '60/011/2015':
    case '33/011/2003':
    case 'OPH-2263-2019':
    case '56/011/2015':
    case 'OPH-4958-2020':
      return Koodistokoodiviite({
        koodiarvo: 'nuortenops',
        koodistoUri: 'lukionoppimaara'
      })
    case '70/011/2015':
    case '4/011/2004':
    case 'OPH-2267-2019':
      return Koodistokoodiviite({
        koodiarvo: 'aikuistenops',
        koodistoUri: 'lukionoppimaara'
      })
  }
}
