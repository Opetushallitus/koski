import React from 'react'
import { useSchema } from '../appstate/constraints'
import { TestIdText } from '../appstate/useTestId'
import { OpiskeluoikeusEditableContext } from '../components-v2/access/RequiresWriteAccess'
import {
  EditorContainer,
  usePäätasonSuoritus
} from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { VirkailijaKansalainenContainer } from '../components-v2/containers/VirkailijaKansalainenContainer'
import { FormModel, useForm } from '../components-v2/forms/FormModel'
import { AdaptedOpiskeluoikeusEditorProps } from '../components-v2/interoperability/useUiAdapter'
import { Spacer } from '../components-v2/layout/Spacer'
import { SuorituksenVahvistusField } from '../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { IBDiplomaOpiskeluoikeus } from '../types/fi/oph/koski/schema/IBDiplomaOpiskeluoikeus'
import { IBDiplomaOpiskeluoikeusjakso } from '../types/fi/oph/koski/schema/IBDiplomaOpiskeluoikeusjakso'
import { Päivämäärävahvistus } from '../types/fi/oph/koski/schema/Paivamaaravahvistus'
import { IBDiplomanOppiaineet } from './IBDiplomanOppiaineet'
import { IBDiplomanSuoritusTiedot } from './IBDiplomanSuoritusTiedot'

export type IBDiplomaEditorProps =
  AdaptedOpiskeluoikeusEditorProps<IBDiplomaOpiskeluoikeus>

// Diploma tuodaan IBIS-tiedostosta, eikä sitä muokata käyttöliittymässä.
// Mitätöinti on silti mahdollista, koska se ei vaadi muokkaustilaa.
// Tilaa, voimassaoloaikaa eikä otsikon vuosia näytetä, koska diploman ainoa
// varsinainen tila on valmistunut vahvistuspäivänä.
export const IBDiplomaEditor: React.FC<IBDiplomaEditorProps> = (props) => {
  const opiskeluoikeusSchema = useSchema('IBDiplomaOpiskeluoikeus')
  const form = useForm(props.opiskeluoikeus, false, opiskeluoikeusSchema)
  const [päätasonSuoritus, setPäätasonSuoritus] = usePäätasonSuoritus(form)

  return (
    <OpiskeluoikeusEditableContext.Provider value={false}>
      <VirkailijaKansalainenContainer opiskeluoikeus={form.state} hidePvmJaTila>
        <EditorContainer
          form={form}
          oppijaOid={props.oppijaOid}
          invalidatable={props.invalidatable}
          suoritusIndex={päätasonSuoritus.index}
          onChangeSuoritus={setPäätasonSuoritus}
          createOpiskeluoikeusjakso={IBDiplomaOpiskeluoikeusjakso}
          additionalOpiskeluoikeusFields={IBOTunniste}
          opiskeluoikeudenTilaEditor={<></>}
          hideOpiskeluoikeusVoimassaoloaika="kaikilta"
          testId={päätasonSuoritus.testId}
        >
          <IBDiplomanSuoritusTiedot suoritus={päätasonSuoritus.suoritus} />
          <Spacer />
          <SuorituksenVahvistusField
            form={form}
            suoritusPath={päätasonSuoritus.path}
            vahvistusClass={Päivämäärävahvistus.className}
          />
          <Spacer />
          <IBDiplomanOppiaineet suoritus={päätasonSuoritus.suoritus} />
        </EditorContainer>
      </VirkailijaKansalainenContainer>
    </OpiskeluoikeusEditableContext.Provider>
  )
}

const IBOTunniste: React.FC<{ form: FormModel<IBDiplomaOpiskeluoikeus> }> = ({
  form
}) => (
  <KeyValueTable>
    <KeyValueRow localizableLabel="Ibo tunniste">
      <TestIdText id="iboTunniste">{form.state.iboTunniste}</TestIdText>
    </KeyValueRow>
  </KeyValueTable>
)
