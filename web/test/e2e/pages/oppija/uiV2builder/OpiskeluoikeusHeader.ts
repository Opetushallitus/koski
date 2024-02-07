import { Button } from './Button'
import { Label } from './Label'
import { OpiskeluoikeudenTila } from './OpiskeluoikeudenTila'

export const OpiskeluoikeusHeader = () => ({
  nimi: Label,
  oid: Label,
  voimassaoloaika: Label,
  edit: Button,
  cancelEdit: Button,
  invalidate: {
    button: Button,
    confirm: Button,
    cancel: Button
  },
  tila: OpiskeluoikeudenTila()
})
