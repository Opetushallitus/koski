import React from 'react'
import { OpenAllButton, useTree } from '../../appstate/tree'
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
  koodinNimiOnly,
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish } from '../../i18n/i18n'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import { isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { deleteAt } from '../../util/array'
import { VSTLisatiedot } from '../common/VSTLisatiedot'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'
import { arviointienPuolestaVahvistettavissa } from '../common/arviointi'
import { isCompletedKoto2012Osasuoritus } from '../common/osasuoritukset'
import { ArviointiProperty } from '../common/propertyFields'
import * as Suoritus from '../common/suoritusFields'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { VSTKoto2012KieliProperties } from './VSTKoto2012KieliProperties'
import { VSTKoto2012ValinnaisetProperties } from './valinnaiset/VSTKoto2012ValinnaisetProperties'
import { VSTKoto2012YhteiskuntaJaTyoelamaosaaminenProperties } from './yhteiskuntajatyoelama/VSTKoto2012YhteiskuntaJaTyoelamaosaaminenProperties'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'

export type VSTKoto2012EditorProps =
  VSTPäätasonSuoritusEditorProps<OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus>

export const VSTKoto2012Editor: React.FC<VSTKoto2012EditorProps> = ({
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
        suorituksenNimi={() => finnish('Kotoutumiskoulutus oppivelvollisille')}
        suorituksetVahvistettu={arviointienPuolestaVahvistettavissa(form.state)}
        createOpiskeluoikeusjakso={createKotoOpiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso"
        testId={päätasonSuoritus.testId}
      >
        <PäätasosuorituksenTiedot>
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
        </PäätasosuorituksenTiedot>
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
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: osasuoritus?.$class,
            format: koodinNimiOnly
          }}
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
        <ArviointiProperty
          form={form}
          path={osasuoritusPath}
          arviointi={
            OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi
          }
        />
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
