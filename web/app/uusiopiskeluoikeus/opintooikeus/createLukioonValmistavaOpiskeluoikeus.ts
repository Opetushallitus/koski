import { Peruste } from '../../appstate/peruste'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LukionOpiskeluoikeudenTila } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeudenTila'
import { LukionOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/LukionOpiskeluoikeusjakso'
import { LukioonValmistavaKoulutus } from '../../types/fi/oph/koski/schema/LukioonValmistavaKoulutus'
import { LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenOpiskeluoikeudenLisatiedot'
import { LukioonValmistavanKoulutuksenOpiskeluoikeus } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenOpiskeluoikeus'
import { LukioonValmistavanKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/LukioonValmistavanKoulutuksenSuoritus'
import { perusteToOppimäärä } from './createLukiokoulutuksenOpiskeluoikeus'
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
