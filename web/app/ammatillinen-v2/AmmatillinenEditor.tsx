import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'

const AmmatillinenEditor: React.FC<
  AdaptedOpiskeluoikeusEditorProps<AmmatillinenOpiskeluoikeus>
> = () => {
  return <div>Hello World</div>
}

export default AmmatillinenEditor
