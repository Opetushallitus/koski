import React from 'react'
import { OpenAllButton, useTree } from '../../appstate/tree'
import { KansalainenOnly } from '../../components-v2/access/KansalainenOnly'
import { EditorContainer } from '../../components-v2/containers/EditorContainer'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import { Spacer } from '../../components-v2/layout/Spacer'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { PäätasonSuorituksenSuostumuksenPeruminen } from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenSuostumuksenPeruminen'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish } from '../../i18n/i18n'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { deleteAt } from '../../util/array'
import { VSTLisatiedot } from '../VSTLisatiedot'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'
import { createArviointi } from '../common/arviointi'
import { isCompletedKoto2012Osasuoritus } from '../common/osasuoritukset'
import { ArviointiProperty } from '../common/propertyFields'
import * as Suoritus from '../common/suoritusFields'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { kaikkiOsasuorituksetVahvistettu } from '../resolvers'
import { VSTKoto2012KieliProperties } from './VSTKoto2012KieliProperties'
import { VSTKoto2012YhteiskuntaJaTyoelamaosaaminenProperties } from './yhteiskuntajatyoelama/VSTKoto2012YhteiskuntaJaTyoelamaosaaminenProperties'
import { VSTKoto2012ValinnaisetProperties } from './valinnaiset/VSTKoto2012ValinnaisetProperties'

export type VSTKoto2012EditorProps =
  VSTPäätasonSuoritusEditorProps<OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus>

export const VSTKoto2012Editor: React.FC<VSTKoto2012EditorProps> = ({
  form,
  oppijaOid,
  päätasonSuoritus,
  invalidatable,
  onChangeSuoritus,
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
        suorituksenNimi={() => finnish('Kotoutumiskoulutus oppivelvollisille')}
        suorituksetVahvistettu={kaikkiOsasuorituksetVahvistettu(form.state)}
        createOpiskeluoikeusjakso={createKotoOpiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso"
      >
        <KansalainenOnly>
          <PäätasonSuorituksenSuostumuksenPeruminen
            opiskeluoikeus={form.state}
            suoritus={päätasonSuoritus.suoritus}
          />
        </KansalainenOnly>
        <Spacer />
        <PäätasosuorituksenTiedot>
          <Suoritus.Oppilaitos form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutus form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Koulutusmoduuli form={form} suoritus={päätasonSuoritus} />
          <Suoritus.Peruste form={form} suoritus={päätasonSuoritus} />
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
          disableAdd={suoritusVahvistettu}
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          editMode={form.editMode}
          completed={isCompletedKoto2012Osasuoritus(päätasonSuoritus.suoritus)}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                form,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path as FormOptic<
                  VapaanSivistystyönOpiskeluoikeus,
                  OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
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

export const createKotoOpiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) =>
  OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
    seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
  )

type OsasuoritusToTableRowParams = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

  const osasuoritus = getValue(osasuoritusPath)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: true,
    columns: {
      Osasuoritus: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaView}
          edit={(arvosanaProps) => (
            <ParasArvosanaEdit
              {...arvosanaProps}
              createArviointi={createArviointi(
                VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022
              )}
            />
          )}
        />
      )
    },
    content:
      isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
        osasuoritus
      ) ? (
        <VSTKoto2012KieliProperties
          form={form}
          // @ts-expect-error
          path={osasuoritusPath}
        />
      ) : isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
          osasuoritus
        ) ? (
        <ArviointiProperty form={form} path={osasuoritusPath} />
      ) : isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
          osasuoritus
        ) ? (
        <VSTKoto2012YhteiskuntaJaTyoelamaosaaminenProperties
          form={form}
          // @ts-expect-error
          osasuoritusPath={osasuoritusPath}
          osasuoritusIndex={osasuoritusIndex}
        />
      ) : isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
          osasuoritus
        ) ? (
        <VSTKoto2012ValinnaisetProperties
          form={form}
          // @ts-expect-error
          osasuoritusPath={osasuoritusPath}
          osasuoritusIndex={osasuoritusIndex}
        />
      ) : undefined
  }
}
