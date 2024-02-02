import React from 'react'
import { useTree } from '../../appstate/tree'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { VapaanSivistystyönOsaamismerkinSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import { EditorContainer } from '../../components-v2/containers/EditorContainer'
import { arviointienPuolestaVahvistettavissa } from '../common/arviointi'
import { finnish } from '../../i18n/i18n'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { KansalainenOnly } from '../../components-v2/access/KansalainenOnly'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import { Spacer } from '../../components-v2/layout/Spacer'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import * as Suoritus from '../common/suoritusFields'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { ArviointiProperty } from '../common/propertyFields'
import { VapaanSivistystyönOsaamismerkinArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinArviointi'
import { FormField } from '../../components-v2/forms/FormField'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { todayISODate } from '../../date/date'
import { KeyValueRow } from '../../components-v2/containers/KeyValueTable'

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
        suorituksenNimi={() => finnish('Vapaan sivistystyön osaamismerkki')}
        suorituksetVahvistettu={arviointienPuolestaVahvistettavissa(form.state)}
        createOpiskeluoikeusjakso={createVstOsaamismerkkiOpiskeluoikeusjakso}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso"
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
            <ArviointiProperty
              form={form}
              path={päätasonSuoritus.path}
              arviointi={VapaanSivistystyönOsaamismerkinArviointi}
            />
          ) : (
            <KeyValueRow label="Arviointi">
              <FormField
                form={form}
                path={päätasonSuoritus.path.prop('arviointi')}
                view={(props) => <ParasArvosanaView {...props} />}
                edit={(props) => (
                  <ParasArvosanaEdit
                    {...props}
                    createArviointi={createOsaamismerkkiArviointi}
                  />
                )}
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
