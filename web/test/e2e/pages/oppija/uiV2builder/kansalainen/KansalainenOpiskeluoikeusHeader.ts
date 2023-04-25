import { Label } from '../Label'
import { KansalainenOpiskeluoikeudenTila } from './KansalainenOpiskeluoikeudenTila'

export const KansalainenOpiskeluoikeusHeader = () => ({
  nimi: Label,
  oid: Label,
  voimassaoloaika: Label,
  tila: KansalainenOpiskeluoikeudenTila()
})
