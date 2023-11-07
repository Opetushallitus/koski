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
import { FormField } from '../../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Spacer } from '../../components-v2/layout/Spacer'
import {
  LaajuusView,
  laajuusSum
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OpintokokonaisuusEdit,
  OpintokokonaisuusView
} from '../../components-v2/opiskeluoikeus/OpintokokonaisuusField'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import {
  ToimipisteEdit,
  ToimipisteView
} from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenToimipiste'
import { OsasuoritusTable } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../../components-v2/opiskeluoikeus/SuorituskieliField'
import {
  TodistuksellaNäkyvätLisätiedotEdit,
  TodistuksellaNäkyvätLisätiedotView
} from '../../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { InfoLink } from '../../components-v2/texts/InfoLink'
import { Trans } from '../../components-v2/texts/Trans'
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
import {
  VSTSuoritus,
  VSTSuoritusPaikallisillaOsasuorituksilla
} from '../common/types'
import { kaikkiOsasuorituksetVahvistettu } from '../resolvers'
import { AddJotpaOsasuoritus } from './AddJotpaOsasuoritus'
import { osasuoritusToTableRow } from './VSTJotpaProperties'

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
        createOpiskeluoikeusjakso={createVstJotpaOpiskeluoikeusjakso} // TODO TOR-2086: Tsekkaa tästä pois mahdollinen turha indirektio
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
          <KeyValueRow
            label="Oppilaitos / toimipiste"
            testId={`${päätasonSuoritus.testId}.toimipiste`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('toimipiste')}
              view={ToimipisteView}
              edit={ToimipisteEdit}
              editProps={{
                onChangeToimipiste: (data: any) => {
                  form.updateAt(
                    päätasonSuoritus.path.prop('toimipiste').optional(),
                    () => data
                  )
                }
              }}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Koulutus"
            testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste`}
          >
            <Trans>
              {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}
            </Trans>
          </KeyValueRow>
          <KeyValueRow
            label="Koulutusmoduuli"
            indent={2}
            testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste.koodiarvo`}
          >
            {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
          </KeyValueRow>
          <KeyValueRow
            label="Opintokokonaisuus"
            indent={2}
            testId={`${päätasonSuoritus.testId}.opintokokonaisuus`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path
                .prop('koulutusmoduuli')
                .prop('opintokokonaisuus')
                .optional()}
              view={OpintokokonaisuusView}
              edit={OpintokokonaisuusEdit}
            />
          </KeyValueRow>
          {/* TODO TOR-2086: Tämä linkki taitaa asemoitua joskus väärin... */}
          <InfoLink koulutusmoduuliClass="fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus" />
          <KeyValueRow label="Laajuus" indent={2}>
            <FormField
              form={form}
              path={päätasonSuoritus.path
                .prop('koulutusmoduuli')
                .prop('laajuus')}
              view={LaajuusView}
              auto={laajuusSum(
                päätasonSuoritus.path
                  .prop('osasuoritukset')
                  .elems()
                  .path('koulutusmoduuli.laajuus'),
                form.state
              )}
              testId={`${päätasonSuoritus.testId}.laajuus`}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Opetuskieli"
            testId={`${päätasonSuoritus.testId}.opetuskieli`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop('suorituskieli')}
              view={SuorituskieliView}
              edit={SuorituskieliEdit}
            />
          </KeyValueRow>
          <KeyValueRow
            label="Todistuksella näkyvät lisätiedot"
            testId={`${päätasonSuoritus.testId}.todistuksella-nakyvat-lisatiedot`}
          >
            <FormField
              form={form}
              path={päätasonSuoritus.path.prop(
                'todistuksellaNäkyvätLisätiedot'
              )}
              view={TodistuksellaNäkyvätLisätiedotView}
              edit={TodistuksellaNäkyvätLisätiedotEdit}
            />
          </KeyValueRow>
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
        <KeyValueTable>
          <KeyValueRow
            label="Yhteensä"
            testId={`${päätasonSuoritus.testId}.yhteensa`}
          >
            {laajuudetYhteensä(päätasonSuoritus.suoritus)}
          </KeyValueRow>
        </KeyValueTable>
      </EditorContainer>
    </TreeNode>
  )
}

const laajuudetYhteensä = (pts: VapaanSivistystyönPäätasonSuoritus): string => {
  const n = formatNumber(
    sum(
      (pts.osasuoritukset || []).map(
        (os) => os.koulutusmoduuli.laajuus?.arvo || 0
      )
    )
  )
  const yksikkö =
    pts.osasuoritukset?.[0]?.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi || ''

  return `${n} ${t(yksikkö)}`
}

export const createVstJotpaOpiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) => {
  return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(seed as any)
}
