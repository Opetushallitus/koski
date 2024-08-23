import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { OpiskeluoikeusClass } from '../../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'

export const opiskeluoikeudenLisätiedotClass = (
  ooClass?: OpiskeluoikeusClass,
  tuvaJärjestämislupa?: Koodistokoodiviite<'tuvajarjestamislupa'>
): string | undefined => {
  if (
    ooClass?.className ===
    TutkintokoulutukseenValmentavanOpiskeluoikeus.className
  ) {
    switch (tuvaJärjestämislupa?.koodiarvo) {
      case 'ammatillinen':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot.className
      case 'lukio':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot.className
      case 'perusopetus':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot.className
      default:
        return undefined
    }
  }

  const lisätiedot = ooClass?.lisätiedot || []
  if (lisätiedot.length > 1) {
    throw new Error(
      `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden lisätietoluokka mahdollinen: ${lisätiedot.join(', ')}`
    )
  }
  return lisätiedot[0]
}
