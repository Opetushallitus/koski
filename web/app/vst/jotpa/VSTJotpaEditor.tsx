import React from 'react'
import { OpenAllButton, useTree } from '../../appstate/tree'
import { KansalainenOnly } from '../../components-v2/access/KansalainenOnly'
import { EditorContainer } from '../../components-v2/containers/EditorContainer'
import { FormOptic } from '../../components-v2/forms/FormModel'
import { Spacer } from '../../components-v2/layout/Spacer'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { OsasuoritusTable } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish } from '../../i18n/i18n'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { deleteAt } from '../../util/array'
import { VSTLisatiedot } from '../common/VSTLisatiedot'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'
import { arviointienPuolestaVahvistettavissa } from '../common/arviointi'
import { isCompletedJotpaOsasuoritus } from '../common/osasuoritukset'
import * as Suoritus from '../common/suoritusFields'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import {
  VSTPäätasonSuoritusEditorProps,
  VSTSuoritusPaikallisillaOsasuorituksilla
} from '../common/types'
import { AddJotpaOsasuoritus } from './AddJotpaOsasuoritus'
import { osasuoritusToTableRow } from './VSTJotpaProperties'

export type VSTJotpaEditorProps =
  VSTPäätasonSuoritusEditorProps<VapaanSivistystyönJotpaKoulutuksenSuoritus>

export const VSTJotpaEditor: React.FC<VSTJotpaEditorProps> = ({
  form,
  oppijaOid,
  päätasonSuoritus,
  invalidatable,
  onChangeSuoritus,
  organisaatio,
  suorituksenVahvistaminenEiMahdollista
}) => {
  const { TreeNode, ...tree } = useTree()

  return (
    <TreeNode>
      <EditorContainer
        form={form}
        invalidatable={invalidatable}
        oppijaOid={oppijaOid}
        suorituksenNimi={() => finnish('Vapaan sivistystyön koulutus')}
        suorituksetVahvistettu={arviointienPuolestaVahvistettavissa(form.state)}
        createOpiskeluoikeusjakso={createVstJotpaOpiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso"
      >
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <PäätasosuorituksenTiedot>
          <Suoritus.Toimipiste form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutusmoduuli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Opintokokonaisuus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Laajuus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Opetuskieli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.TodistuksenLisätiedot
            form={form}
            suoritus={päätasonSuoritus}
          />
        </PäätasosuorituksenTiedot>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suorituksenVahvistaminenEiMahdollista}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          editMode={form.editMode}
          addNewOsasuoritusView={AddJotpaOsasuoritus}
          addNewOsasuoritusViewProps={{
            form,
            osasuoritusPath: päätasonSuoritus.path
          }}
          completed={isCompletedJotpaOsasuoritus(päätasonSuoritus.suoritus)}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                level: 0,
                form,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path as FormOptic<
                  VapaanSivistystyönOpiskeluoikeus,
                  VSTSuoritusPaikallisillaOsasuorituksilla
                >
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
