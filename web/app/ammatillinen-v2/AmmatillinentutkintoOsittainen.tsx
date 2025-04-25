import React from 'react'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import { getValue, useForm } from '../components-v2/forms/FormModel'
import { useSchema } from '../appstate/constraints'
import { UusiOpiskeluoikeusjakso } from '../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { AmmatillinenOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeusjakso'
import { useTree } from '../appstate/tree'
import { AmmatillinenLisatiedot } from './AmmatillinenLisatiedot'
import { OphButton } from '@opetushallitus/oph-design-system'

const AmmatillinenTutkintoOsittainenEditor: React.FC<
  AdaptedOpiskeluoikeusEditorProps<AmmatillinenOpiskeluoikeus>
> = (props) => {
  const opiskeluoikeusSchema = useSchema(AmmatillinenOpiskeluoikeus.className)
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  const createAmmatillinenOpiskeluoikeusJakso = (
    seed: UusiOpiskeluoikeusjakso<AmmatillinenOpiskeluoikeusjakso>
  ) => AmmatillinenOpiskeluoikeusjakso(seed)

  const { TreeNode, ...tree } = useTree()



  return (
    <TreeNode>
      <EditorContainer
        form={form}
        oppijaOid={props.oppijaOid}
        invalidatable={props.invalidatable}
        onChangeSuoritus={setPäätasonSuoritus}
        testId={päätasonSuoritus.testId}
        createOpiskeluoikeusjakso={createAmmatillinenOpiskeluoikeusJakso}
        lisätiedotContainer={AmmatillinenLisatiedot}
      />
      <OphButton>Hello</OphButton>
    </TreeNode>
  )
}

export default AmmatillinenTutkintoOsittainenEditor
