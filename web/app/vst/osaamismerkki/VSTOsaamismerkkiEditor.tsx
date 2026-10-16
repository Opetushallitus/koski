import React from 'react'
import { useTree } from '../../appstate/tree'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { VapaanSivistystyönOsaamismerkinSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import { EditorContainer } from '../../components-v2/containers/EditorContainer'
import { arviointienPuolestaVahvistettavissa } from '../common/arviointi'
import { localize, t } from '../../i18n/i18n'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { KansalainenOnly } from '../../components-v2/access/KansalainenOnly'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { Spacer } from '../../components-v2/layout/Spacer'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import * as Suoritus from '../common/suoritusFields'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { VapaanSivistystyönOsaamismerkinArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinArviointi'
import {
  FieldEditorProps,
  FieldViewerProps,
  FormField
} from '../../components-v2/forms/FormField'
import {
  ArvosanaEdit,
  ArvosanaView,
  koodinNimiOnly,
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { todayISODate } from '../../date/date'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
import { FormListField } from '../../components-v2/forms/FormListField'
import { DateEdit, DateView } from '../../components-v2/controls/DateField'
import { TestIdLayer } from '../../appstate/useTestId'
import { EmptyObject } from '../../util/objects'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'

export type VSTOsaamismerkkiEditor =
  VSTPäätasonSuoritusEditorProps<VapaanSivistystyönOsaamismerkinSuoritus>

export const VSTOsaamismerkkiEditor: React.FC<VSTOsaamismerkkiEditor> = ({
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
        suorituksenNimi={() => localize(t('Vapaan sivistystyön osaamismerkki'))}
        suorituksetVahvistettu={arviointienPuolestaVahvistettavissa(form.state)}
        createOpiskeluoikeusjakso={createVstOsaamismerkkiOpiskeluoikeusjakso}
        suoritusIndex={päätasonSuoritus.index}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso"
        testId={päätasonSuoritus.testId}
      >
        <Spacer />
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <PäätasosuorituksenTiedot>
          <Suoritus.Toimipiste form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Osaamismerkki form={form} suoritus={päätasonSuoritus} />

          {päätasonSuoritus.suoritus.arviointi !== undefined &&
          päätasonSuoritus.suoritus.arviointi.length > 0 ? (
            <KeyValueRow localizableLabel="Arviointi">
              <TestIdLayer id="arviointi">
                <FormListField
                  form={form}
                  path={päätasonSuoritus.path.prop('arviointi')}
                  view={OsaamismerkinArviointiView}
                  edit={OsaamismerkinArviointiEdit}
                  editProps={{
                    suoritusClassName: päätasonSuoritus.suoritus.$class
                  }}
                />
              </TestIdLayer>
            </KeyValueRow>
          ) : (
            <KeyValueRow localizableLabel="Arviointi">
              <FormField
                form={form}
                path={päätasonSuoritus.path.prop('arviointi')}
                view={(props) => <ParasArvosanaView {...props} />}
                edit={ParasArvosanaEdit}
                editProps={{
                  suoritusClassName: päätasonSuoritus.suoritus.$class
                }}
                testId="arvosana"
              />
            </KeyValueRow>
          )}
        </PäätasosuorituksenTiedot>
        <Spacer />
        <SuorituksenVahvistusField
          form={form}
          suoritusPath={päätasonSuoritus.path}
          organisaatio={organisaatio}
          disableAdd={suorituksenVahvistaminenEiMahdollista}
          disableRemoval={false}
          vahvistusClass={HenkilövahvistusValinnaisellaPaikkakunnalla.className}
        />
        <Spacer />
      </EditorContainer>
    </TreeNode>
  )
}

const createVstOsaamismerkkiOpiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) =>
  VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso(
    seed as UusiOpiskeluoikeusjakso<VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso>
  )

const createOsaamismerkkiArviointi = (
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
): VapaanSivistystyönOsaamismerkinArviointi =>
  VapaanSivistystyönOsaamismerkinArviointi({
    arvosana,
    päivä: todayISODate()
  })

const OsaamismerkinArviointiView: React.FC<
  FieldViewerProps<VapaanSivistystyönOsaamismerkinArviointi, EmptyObject>
> = ({ value }) =>
  value ? (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        <ArvosanaView value={value} />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Päivämäärä" innerKeyValueTable>
        <DateView value={value.päivä} />
      </KeyValueRow>
    </KeyValueTable>
  ) : null

const OsaamismerkinArviointiEdit: React.FC<
  FieldEditorProps<
    VapaanSivistystyönOsaamismerkinArviointi,
    { suoritusClassName: string }
  >
> = (props) => {
  const { value, onChange, errors } = props
  return value ? (
    <KeyValueTable>
      <KeyValueRow localizableLabel="Arvosana" innerKeyValueTable>
        <ArvosanaEdit {...props} format={koodinNimiOnly} />
      </KeyValueRow>
      <KeyValueRow localizableLabel="Päivämäärä" innerKeyValueTable>
        <DateEdit
          value={value.päivä}
          onChange={(päivä) => päivä && onChange({ ...value, päivä })}
          errors={errors}
        />
      </KeyValueRow>
    </KeyValueTable>
  ) : null
}
