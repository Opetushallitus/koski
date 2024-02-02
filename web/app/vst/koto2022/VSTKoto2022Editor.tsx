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
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView,
  laajuusSum
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { SuorituksenVahvistusField } from '../../components-v2/opiskeluoikeus/SuorituksenVahvistus'
import { UusiOpiskeluoikeusjakso } from '../../components-v2/opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { finnish } from '../../i18n/i18n'
import { OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import { VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
import { isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeusjakso'
import { deleteAt } from '../../util/array'
import { VSTLisatiedot } from '../common/VSTLisatiedot'
import { VSTLaajuudetYhteensä } from '../common/VSTLaajuudetYhteensa'
import {
  createArviointi,
  arviointienPuolestaVahvistettavissa
} from '../common/arviointi'
import { isCompletedKoto2022Osasuoritus } from '../common/osasuoritukset'
import * as Suoritus from '../common/suoritusFields'
import { PäätasosuorituksenTiedot } from '../common/suoritusFields'
import { VSTPäätasonSuoritusEditorProps } from '../common/types'
import { AddKoto2022Osasuoritus } from './AddKoto2022Osasuoritus'
import { VSTKoto2022KieliJaViestintaProperties } from './kielijaviestinta/VSTKoto2022KieliJaViestintaProperties'
import { VSTKoto2022ValinnaisetProperties } from './valinnaiset/VSTKoto2022ValinnaisetProperties'
import { VSTKoto2022YhteiskuntaJaTyoelamaosaaminenProperties } from './yhteiskuntajatyoelama/VSTKoto2022YhteiskuntaJaTyoelamaosaaminenProperties'

export type VSTKoto2022EditorProps =
  VSTPäätasonSuoritusEditorProps<OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022>

export const VSTKoto2022Editor: React.FC<VSTKoto2022EditorProps> = ({
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
        createOpiskeluoikeusjakso={createKoto2022Opiskeluoikeusjakso}
        lisätiedotContainer={VSTLisatiedot}
        onChangeSuoritus={onChangeSuoritus}
        opiskeluoikeusJaksoClassName="fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso"
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
        />
        <Spacer />

        {päätasonSuoritus.suoritus.osasuoritukset && (
          <OpenAllButton {...tree} />
        )}

        <Spacer />
        <OsasuoritusTable
          editMode={form.editMode}
          addNewOsasuoritusView={AddKoto2022Osasuoritus}
          addNewOsasuoritusViewProps={{
            form,
            osasuoritusPath: päätasonSuoritus.path
          }}
          completed={isCompletedKoto2022Osasuoritus(päätasonSuoritus.suoritus)}
          rows={(päätasonSuoritus.suoritus.osasuoritukset || []).map(
            (_os, osasuoritusIndex) =>
              osasuoritusToTableRow({
                form,
                osasuoritusIndex,
                suoritusIndex: päätasonSuoritus.index,
                suoritusPath: päätasonSuoritus.path as FormOptic<
                  VapaanSivistystyönOpiskeluoikeus,
                  OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
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

export const createKoto2022Opiskeluoikeusjakso = (
  seed: UusiOpiskeluoikeusjakso<VapaanSivistystyönOpiskeluoikeusjakso>
) =>
  OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(
    seed as UusiOpiskeluoikeusjakso<OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso>
  )

type OsasuoritusToTableRowParams = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
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

  const ohjauksenOsasuoritus =
    isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(osasuoritus)

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
      Laajuus:
        isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
          osasuoritus
        ) ||
        isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
          osasuoritus
        ) ||
        isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
          osasuoritus
        ) ? (
          <FormField
            form={form}
            path={osasuoritusPath.path('koulutusmoduuli.laajuus')}
            view={LaajuusView}
            auto={laajuusSum(
              osasuoritusPath
                // @ts-expect-error
                .prop('osasuoritukset')
                .optional()
                .elems()
                .path('koulutusmoduuli.laajuus'),
              form.state
            )}
          />
        ) : (
          <FormField
            form={form}
            path={osasuoritusPath.path('koulutusmoduuli.laajuus')}
            view={LaajuusView}
            edit={LaajuusOpintopisteissäEdit}
          />
        ),
      Arvosana: ohjauksenOsasuoritus ? null : (
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
    content: isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(
      osasuoritus
    ) ? (
      <VSTKoto2022KieliJaViestintaProperties
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritusPath}
      />
    ) : isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(
        osasuoritus
      ) ? (
      <VSTKoto2022YhteiskuntaJaTyoelamaosaaminenProperties
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritusPath}
      />
    ) : isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(
        osasuoritus
      ) ? (
      <VSTKoto2022ValinnaisetProperties
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritusPath}
      />
    ) : undefined
  }
}
