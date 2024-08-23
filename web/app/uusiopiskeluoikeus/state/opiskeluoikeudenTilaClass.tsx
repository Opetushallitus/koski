import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { OpiskeluoikeusClass } from '../../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'

export const opiskeluoikeudenTilaClass = (
  ooClass?: OpiskeluoikeusClass,
  suoritustyyppi?: string
): string | undefined => {
  switch (ooClass?.className) {
    case VapaanSivistystyönOpiskeluoikeus.className:
      switch (suoritustyyppi) {
        case 'vstoppivelvollisillesuunnattukoulutus':
        case 'vstmaahanmuuttajienkotoutumiskoulutus':
        case 'vstlukutaitokoulutus':
          return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
        case 'vstjotpakoulutus':
          return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
        case 'vstosaamismerkki':
          return VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className
        case 'vstvapaatavoitteinenkoulutus':
          return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className
        default:
          return undefined
      }
    default: {
      const jaksot = ooClass?.opiskeluoikeusjaksot || []
      if (jaksot.length > 1) {
        throw new Error(
          `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden tilan luokka mahdollinen: ${ooClass?.className}: ${jaksot.join(', ')}`
        )
      }
      return jaksot[0]
    }
  }
}
