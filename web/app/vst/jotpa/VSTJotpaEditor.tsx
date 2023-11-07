import React from 'react'
import { OpenAllButton, useTree } from '../../appstate/tree'
import { CommonProps } from '../../components-v2/CommonProps'
import { KansalainenOnly } from '../../components-v2/access/KansalainenOnly'
import {
  ActivePäätasonSuoritus,
  EditorContainer
} from '../../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Spacer } from '../../components-v2/layout/Spacer'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OsasuoritusTable } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish, t } from '../../i18n/i18n'
import { Koulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { deleteAt } from '../../util/array'
import { formatNumber, sum } from '../../util/numbers'
import { VSTLisatiedot } from '../VSTLisatiedot'
import * as Suoritus from '../common/suoritusFields'
import {
  VSTSuoritus,
  VSTSuoritusPaikallisillaOsasuorituksilla
} from '../common/types'
import { kaikkiOsasuorituksetVahvistettu } from '../resolvers'
import { AddJotpaOsasuoritus } from './AddJotpaOsasuoritus'
import { osasuoritusToTableRow } from './VSTJotpaProperties'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'

// TODO TOR-2086: Tee tästä yleinen tyyppi, jonka kaikki vst-editorit spesfoivat
export type VSTJotpaEditorProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  oppijaOid: string
  päätasonSuoritus: ActivePäätasonSuoritus<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenSuoritus
  >
  organisaatio?: Koulutustoimija | Oppilaitos
  invalidatable: boolean
  onChangeSuoritus: (suoritusIndex: number) => void
  onCreateOsasuoritus: (
    suoritusPath: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTSuoritus
  ) => void // TODO TOR-2086: Tyypitys
  suoritusVahvistettu: boolean
}>

export const VSTJotpaEditor: React.FC<VSTJotpaEditorProps> = ({
  form,
  oppijaOid,
  päätasonSuoritus,
  invalidatable,
  onChangeSuoritus,
  onCreateOsasuoritus,
  organisaatio,
  suoritusVahvistettu
}) => {
  const { TreeNode, ...tree } = useTree()

  return (
    <TreeNode>
      <EditorContainer
        form={form}
        invalidatable={invalidatable}
        oppijaOid={oppijaOid}
        suorituksenNimi={() => finnish('Vapaan sivistystyön koulutus')}
        suorituksetVahvistettu={kaikkiOsasuorituksetVahvistettu(form.state)}
        createOpiskeluoikeusjakso={createVstJotpaOpiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        testId={`${päätasonSuoritus.testId}.editor-container`}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso"
      >
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <KeyValueTable>
          <Suoritus.Oppilaitos form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutusmoduuli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Opintokokonaisuus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Laajuus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Opetuskieli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.TodistuksenLisätiedot
            form={form}
            suoritus={päätasonSuoritus}
          />
        </KeyValueTable>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suoritusVahvistettu}
          testId={päätasonSuoritus.testId}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          testId={päätasonSuoritus.testId}
          editMode={form.editMode}
          addNewOsasuoritusView={AddJotpaOsasuoritus}
          addNewOsasuoritusViewProps={{
            form,
            osasuoritusPath: päätasonSuoritus.path
          }}
          completed={(rowIndex) => {
            const osasuoritus = (päätasonSuoritus.suoritus.osasuoritukset ||
              [])[rowIndex]
            if (!osasuoritus) {
              return false
            }
            return (
              osasuoritus.arviointi !== undefined &&
              osasuoritus.arviointi.length > 0
            )
          }}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                level: 0,
                form,
                createOsasuoritus: onCreateOsasuoritus,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path as FormOptic<
                  VapaanSivistystyönOpiskeluoikeus,
                  VSTSuoritusPaikallisillaOsasuorituksilla
                >,
                testId: `${päätasonSuoritus.testId}.osasuoritukset.${osasuoritusIndex}`
              })
          )}
          onRemove={(i) => {
            form.updateAt(
              päätasonSuoritus.path.prop('osasuoritukset').optional(),
              deleteAt(i)
            )
          }}
        />
        <VSTLaajuudetYhteensä
          suoritus={päätasonSuoritus.suoritus}
          testId={päätasonSuoritus.testId}
        />
      </EditorContainer>
    </TreeNode>
  )
}

export const createVstJotpaOpiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) =>
  VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(
    seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso>
  )
