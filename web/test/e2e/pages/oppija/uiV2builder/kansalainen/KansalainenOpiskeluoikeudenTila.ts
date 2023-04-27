import { arrayOf } from '../builder'
import { Label } from '../Label'

export const KansalainenOpiskeluoikeudenTila = () => ({
  value: {
    items: arrayOf({
      date: Label,
      tila: Label
    })
  }
})
