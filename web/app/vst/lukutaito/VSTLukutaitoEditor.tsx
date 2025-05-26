import React from 'react'
import { OpenAllButton, useTree } from '../../appstate/tree'
import { EditorContainer } from '../../components-v2/containers/EditorContainer'
import { FormOptic } from '../../components-v2/forms/FormModel'
import { Spacer } from '../../components-v2/layout/Spacer'
import { OsasuoritusTable } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish } from '../../i18n/i18n'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { deleteAt } from '../../util/array'
import { VSTLisatiedot } from '../common/VSTLisatiedot'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'
import { arviointienPuolestaVahvistettavissa } from '../common/arviointi'
import { isCompletedLukutaitokoulutuksenOsasuoritus } from '../common/osasuoritukset'
import * as Suoritus from '../common/suoritusFields'
import {
  VSTPäätasonSuoritusEditorProps,
  VSTSuoritusOsasuorituksilla
} from '../common/types'
import { AddLukutaitoOsasuoritus } from './AddLukutaitoOsasuoritus'
import { osasuoritusToTableRow } from './VSTLukutaitoProperties'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'

export type VSTLukutaitoEditorProps =
  VSTPäätasonSuoritusEditorProps<VapaanSivistystyönLukutaitokoulutuksenSuoritus>

export const VSTLukutaitoEditor: React.FC<VSTLukutaitoEditorProps> = ({
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
        suorituksenNimi={() => finnish('Lukutaitokoulutus oppivelvollisille')}
        suorituksetVahvistettu={arviointienPuolestaVahvistettavissa(form.state)}
        createOpiskeluoikeusjakso={createVstLukutaitoOpiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso"
        testId={päätasonSuoritus.testId}
      >
        <Suoritus.PäätasosuorituksenTiedot>
          <Suoritus.Toimipiste form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutusmoduuli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Peruste form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Laajuus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Opetuskieli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.TodistuksenLisätiedot
            form={form}
            suoritus={päätasonSuoritus}
          />
        </Suoritus.PäätasosuorituksenTiedot>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suorituksenVahvistaminenEiMahdollista}
          vahvistusClass={HenkilövahvistusValinnaisellaPaikkakunnalla.className}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          editMode={form.editMode}
          addNewOsasuoritusView={AddLukutaitoOsasuoritus}
          addNewOsasuoritusViewProps={{
            form,
            osasuoritusPath: päätasonSuoritus.path
          }}
          completed={isCompletedLukutaitokoulutuksenOsasuoritus(
            päätasonSuoritus.suoritus
          )}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                level: 0,
                form,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path as FormOptic<
                  VapaanSivistystyönOpiskeluoikeus,
                  VSTSuoritusOsasuorituksilla
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

export const createVstLukutaitoOpiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) =>
  OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
    seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
  )
