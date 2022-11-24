import React from 'baret'
import { tutkinnonOsanRyhmät } from '../koodisto/koodistot'
import { t } from '../i18n/i18n'
import {
  modelData,
  modelLookup,
  modelProperties,
  modelProperty,
  modelTitle,
  optionalPrototypeModel
} from '../editor/EditorModel'
import * as R from 'ramda'
import {
  createTutkinnonOsanSuoritusPrototype,
  NON_GROUPED,
  osanOsa
} from '../ammatillinen/TutkinnonOsa'
import { hasArvosana, suorituksenTyyppi, tilaText } from './Suoritus'
import Text from '../i18n/Text'
import { ArvosanaEditor } from './ArvosanaEditor'
import { Editor } from '../editor/Editor'
import { shouldShowProperty } from '../editor/PropertiesEditor'
import { sortLanguages } from '../util/sorting'
import { suorituksenTilaSymbol } from './Suoritustaulukko'
import { isKieliaine } from './Koulutusmoduuli'
import {
  EditorModel,
  isSomeOptionalModel,
  ObjectModel,
  ObjectModelProperty,
  OptionalModel,
  PrototypeModel
} from '../types/EditorModels'
import { BaseContext, Contextualized } from '../types/EditorModelContext'
import { Bus, Observable } from 'baconjs'
import classNames from 'classnames'
import { eshSuorituksenTyyppi } from '../esh/europeanschoolofhelsinkiSuoritus'

export type SuoritusModel<T extends object = {}> = ObjectModel &
  OptionalModel &
  Contextualized<T & SuoritusContext>
export type SuoritusContext = {
  suoritus: SuoritusModel
}
export type OnExpandFn = (expanded: boolean) => void

export const isAmmatillinentutkinto = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('ammatillisentutkinnonsuoritus')
export const isMuunAmmatillisenKoulutuksenSuoritus = (
  suoritus: SuoritusModel
) =>
  suoritus &&
  suoritus.value.classes.includes('muunammatillisenkoulutuksensuoritus')
export const isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (
  suoritus: SuoritusModel
) =>
  suoritus.value.classes.includes(
    'muunammatillisenkoulutuksenosasuorituksensuoritus'
  )
export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = (
  suoritus: SuoritusModel
) =>
  suoritus.value.classes.includes(
    'tutkinnonosaapienemmistakokonaisuuksistakoostuvasuoritus'
  )
export const isNäyttötutkintoonValmistava = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes(
    'nayttotutkintoonvalmistavankoulutuksensuoritus'
  )
export const isYlioppilastutkinto = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('ylioppilastutkinnonsuoritus')
export const isVapaanSivistystyönOppivelvollistenSuoritus = (
  suoritus: SuoritusModel
) =>
  suoritus.value.classes.includes(
    'oppivelvollisillesuunnattuvapaansivistystyonkoulutuksensuoritus'
  )
export const isEshPäätasonSuoritus = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('europeanschoolofhelsinkipäätasonsuoritus')
export const isEB = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('ebtutkinnonsuoritus')
export const isEshS7 = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('secondaryuppervuosiluokansuoritus') &&
  modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo') === 'S7'
export const isEshS6 = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('secondaryuppervuosiluokansuoritus') &&
  modelData(suoritus, 'koulutusmoduuli.tunniste.koodiarvo') === 'S6'
export const isEshOsasuoritus = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes('europeanschoolofhelsinkiosasuoritus')

export const isMaahanmuuttajienKotoutumiskoulutuksenSuoritus = (
  suoritus: SuoritusModel
) =>
  suoritus.value.classes.includes(
    'oppivelvollisillesuunnattumaahanmuuttajienkotoutumiskoulutuksensuoritus'
  )
export const isLukutaitokoulutuksenSuoritus = (suoritus: SuoritusModel) =>
  suoritus.value.classes.includes(
    'vapaansivistystyonlukutaitokoulutuksensuoritus'
  )

export const getLaajuusYksikkö = <
  T extends EditorModel & OptionalModel & Contextualized
>(
  suoritus?: T
): T => {
  const laajuusModel = modelLookup(suoritus, 'koulutusmoduuli.laajuus')
  return laajuusModel &&
    isSomeOptionalModel(laajuusModel) &&
    !modelData(laajuusModel)
    ? t(modelData(optionalPrototypeModel(laajuusModel), 'yksikkö.lyhytNimi'))
    : t(modelData(laajuusModel, 'yksikkö.lyhytNimi'))
}

// Can be called without suoritusProto if suoritukset has always at least one member
export const groupSuoritukset = (
  parentSuoritus: SuoritusModel,
  suoritukset: SuoritusModel[],
  context: BaseContext,
  suoritusProto: PrototypeModel
) => {
  const diaarinumero = modelData(
    parentSuoritus,
    'koulutusmoduuli.perusteenDiaarinumero'
  )
  const suoritustapa = modelData(parentSuoritus, 'suoritustapa')

  return tutkinnonOsanRyhmät(diaarinumero, suoritustapa).map(
    // @ts-expect-error
    (ammatillisentutkinnonosanryhmaKoodisto) => {
      let grouped, groupIds, groupTitles
      if (
        isAmmatillinentutkinto(parentSuoritus) &&
        R.keys(ammatillisentutkinnonosanryhmaKoodisto).length > 1
      ) {
        grouped = R.groupBy(
          // @ts-expect-error
          (s) => modelData(s, 'tutkinnonOsanRyhmä.koodiarvo') || NON_GROUPED
        )(suoritukset)
        groupTitles = R.mergeRight(ammatillisentutkinnonosanryhmaKoodisto, {
          [NON_GROUPED]: t('Muut suoritukset')
        })
        groupIds = R.keys(grouped).sort()
        if (context.edit) {
          // Show the empty groups too
          groupIds = R.uniq(
            R.keys(ammatillisentutkinnonosanryhmaKoodisto).concat(groupIds)
          )
        }
      } else {
        // Osasuorituksia voi olla monta tasoa (osasuorituksen osasuorituksia), jolloin on suoraviivaisempaa
        // tarkistaa ylimmän tason suorituksesta, onko kyseessä muun ammatillisen koulutksen tai tutkinnon
        // osaa pienemmistä kokonaisuuksista koostuva suoritus.
        const topLevelSuoritus = R.path(['context', 'suoritus'], suoritusProto)
        if (
          topLevelSuoritus &&
          // @ts-expect-error
          (isMuunAmmatillisenKoulutuksenSuoritus(topLevelSuoritus) ||
            isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
              // @ts-expect-error
              topLevelSuoritus
            ))
        ) {
          grouped = { [NON_GROUPED]: suoritukset }
          groupTitles = { [NON_GROUPED]: t('Osasuoritus') }
          groupIds = [NON_GROUPED]
        } else {
          // console.log("suoritukset[0]", suoritukset[0])
          // console.log("suoritusProto", suoritusProto)
          grouped = { [NON_GROUPED]: suoritukset }
          groupTitles = {
            [NON_GROUPED]: t(
              modelProperty(suoritukset[0] || suoritusProto, 'koulutusmoduuli')!
                .title
            )
          }
          groupIds = [NON_GROUPED]
        }
      }

      return {
        grouped: grouped,
        groupTitles: groupTitles,
        groupIds: groupIds
      }
    }
  )
}

export const suoritusProperties = (
  suoritus: SuoritusModel,
  complexArviointi?: SuoritusModel
) => {
  const filterProperties = (filter: (p: ObjectModelProperty) => boolean) =>
    modelProperties(suoritus, filter)
  const includeProperties = (...properties: string[]) =>
    filterProperties((p) => properties.includes(p.key))
  const excludeProperties = (...properties: string[]) =>
    filterProperties((p) => !properties.includes(p.key))

  const propertiesForSuoritustyyppi = (tyyppi: string, isEdit: boolean) => {
    const simplifiedArviointi = modelProperties(
      modelLookup(suoritus, 'arviointi.-1')!,
      (p: ObjectModelProperty) =>
        !['arvosana', 'arvioitsijat', 'pisteet', 'kuvaus'].includes(p.key)
    )

    const arvioitsijat = modelProperties(
      modelLookup(suoritus, 'arviointi.-1')!,
      (p: ObjectModelProperty) => p.key === 'arvioitsijat'
    )

    const arvioinninKuvaus = modelProperties(
      modelLookup(suoritus, 'arviointi.-1')!,
      (p: ObjectModelProperty) => p.key === 'kuvaus'
    )
    const arviointipäivä = modelProperties(
      modelLookup(suoritus, 'arviointi.-1')!,
      (p: ObjectModelProperty) => p.key === 'päivä'
    )
    const showPakollinen =
      tyyppi !== 'nayttotutkintoonvalmistavakoulutus' &&
      modelData(suoritus, 'koulutusmoduuli.pakollinen') !== undefined
    const pakollinen = showPakollinen
      ? modelProperties(
          modelLookup(suoritus, 'koulutusmoduuli')!,
          (p: ObjectModelProperty) => p.key === 'pakollinen'
        )
      : []
    const arviointi = modelProperties(suoritus).filter(
      (p) => p.key === 'arviointi'
    )

    const taitotasot = modelProperties(
      modelLookup(suoritus, 'arviointi.-1')!,
      (p: ObjectModelProperty) =>
        isEdit &&
        [
          'kuullunYmmärtämisenTaitotaso',
          'puhumisenTaitotaso',
          'luetunYmmärtämisenTaitotaso',
          'kirjoittamisenTaitotaso'
        ].includes(p.key)
    )

    const defaultsForEdit = pakollinen
      .concat(complexArviointi ? arviointi : arviointipäivä)
      .concat(taitotasot)
      .concat(
        includeProperties(
          'näyttö',
          'tunnustettu',
          'lisätiedot',
          'liittyyTutkinnonOsaan'
        )
      )

    const defaultsForView = pakollinen
      .concat(
        excludeProperties(
          'koulutusmoduuli',
          'arviointi',
          'tutkinnonOsanRyhmä',
          'tutkintokerta'
        )
      )
      .concat(taitotasot)
      .concat(complexArviointi ? arviointi : simplifiedArviointi)

    switch (tyyppi) {
      case 'valma':
      case 'telma':
        return isEdit
          ? pakollinen
              .concat(includeProperties('näyttö', 'tunnustettu', 'lisätiedot'))
              .concat(arviointipäivä)
              .concat(arvioinninKuvaus)
              .concat(simplifiedArviointi)
          : defaultsForView
      case eshSuorituksenTyyppi.secondaryUpper:
      case eshSuorituksenTyyppi.secondaryLower:
      case eshSuorituksenTyyppi.nursery:
      case eshSuorituksenTyyppi.primary:
      case eshSuorituksenTyyppi.ebtutkinto:
        return defaultsForView
          .concat(arvioitsijat)
          .concat(arvioinninKuvaus)
          .filter((p) => p.key !== 'suorituskieli')
      default:
        return isEdit ? defaultsForEdit : defaultsForView
    }
  }

  const kuvaus = modelProperties(
    modelLookup(suoritus, 'koulutusmoduuli')!,
    (p: ObjectModelProperty) => p.key === 'kuvaus'
  )
  const parentSuorituksenTyyppi = suorituksenTyyppi(suoritus.context.suoritus)

  return kuvaus
    .concat(
      propertiesForSuoritustyyppi(
        parentSuorituksenTyyppi,
        Boolean(suoritus.context.edit)
      )
    )
    .filter(shouldShowProperty(suoritus.context))
}

export type ExpandAllRowsProps = {
  allExpandedP: Observable<boolean>
  toggleExpandAll: Bus<boolean, boolean>
}

export const ExpandAllRows = ({
  allExpandedP,
  toggleExpandAll
}: ExpandAllRowsProps) => (
  <thead>
    <tr>
      <th className="suoritus">
        <div>
          {/* @ts-expect-error */}
          {allExpandedP.map((allExpanded) => (
            <button
              className={classNames('expand-all', 'koski-button', {
                expanded: allExpanded
              })}
              aria-label={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'}
              // @ts-expect-error
              onClick={toggleExpandAll}
            >
              {/* @ts-expect-error */}
              <Text name={allExpanded ? 'Sulje kaikki' : 'Avaa kaikki'} />
            </button>
          ))}
        </div>
      </th>
    </tr>
  </thead>
)

/*
 *
 * Shared column types
 *
 */

export type ColumnIface<
  D extends object,
  S extends object | void = void,
  H extends object | void = void
> = {
  shouldShow: (props: S) => boolean
  renderHeader: (props: H) => any
  renderData: (props: D) => any
}

// SuoritusColumn

export type SuoritusColumn = ColumnIface<
  SuoritusColumnDataProps,
  {},
  SuoritusColumnHeaderProps
>

export type SuoritusColumnHeaderProps = {
  suoritusTitle: string
}

export type SuoritusColumnDataProps = {
  model: SuoritusModel
  showTila: boolean
  onExpand: OnExpandFn
  hasProperties: boolean
  expanded: boolean
}

export const SuoritusColumn: SuoritusColumn = {
  shouldShow: () => true,
  renderHeader: ({ suoritusTitle }) => (
    <td key="suoritus" className="suoritus">
      {/* @ts-expect-error */}
      <Text name={suoritusTitle} />
    </td>
  ),
  renderData: ({ model, showTila, onExpand, hasProperties, expanded }) => {
    const koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')
    const titleAsExpandLink =
      hasProperties && (!osanOsa(koulutusmoduuli) || !model.context.edit)
    const kieliaine = isKieliaine(koulutusmoduuli)
    const koulutusmoduuliTunniste = modelData(koulutusmoduuli, 'tunniste.nimi')

    return (
      <td key="suoritus" className="suoritus">
        <a
          className={hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
          onClick={() => onExpand(!expanded)}
          role="button"
          aria-expanded={expanded}
          aria-label={
            expanded
              ? `Pienennä suoritus ${t(koulutusmoduuliTunniste)}`
              : `Laajenna suoritus ${t(koulutusmoduuliTunniste)}`
          }
        >
          {expanded ? <>&#61766;</> : <>&#61694;</>}
        </a>
        {showTila && (
          <span className="tila" title={tilaText(model)}>
            {suorituksenTilaSymbol(model)}
          </span>
        )}
        {titleAsExpandLink ? (
          <button
            className="nimi inline-link-button"
            onClick={() => onExpand(!expanded)}
          >
            {modelTitle(model, 'koulutusmoduuli')}
          </button>
        ) : (
          <span className="nimi">
            {t(modelData(koulutusmoduuli, 'tunniste.nimi')) +
              (kieliaine ? ', ' : '')}
            {kieliaine && (
              <span className="value kieli">
                <Editor
                  model={koulutusmoduuli}
                  inline={true}
                  path="kieli"
                  sortBy={sortLanguages}
                />
              </span>
            )}
          </span>
        )}
      </td>
    )
  }
}

// TutkintokertaColumn

export type TutkintokertaColumn = ColumnIface<
  TutkintokertaColumnDataProps,
  TutkintokertaColumnShowProps
>

export type TutkintokertaColumnShowProps = {
  parentSuoritus: SuoritusModel
}

export type TutkintokertaColumnDataProps = {
  model: SuoritusModel
}

export const TutkintokertaColumn: TutkintokertaColumn = {
  shouldShow: ({ parentSuoritus }) => isYlioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return (
      <th key="tutkintokerta" className="tutkintokerta" scope="col">
        {/* @ts-expect-error */}
        <Text name="Tutkintokerta" />
      </th>
    )
  },
  renderData: ({ model }) => (
    <td key="tutkintokerta" className="tutkintokerta">
      <Editor model={model} path="tutkintokerta.vuosi" compact="true" />{' '}
      <Editor model={model} path="tutkintokerta.vuodenaika" compact="true" />
    </td>
  )
}

// KoepisteetColumn

export type KoepisteetColumn = ColumnIface<
  KoepisteetColumnDataProps,
  KoepisteetColumnShowProps
>

export type KoepisteetColumnShowProps = {
  parentSuoritus: SuoritusModel
}

export type KoepisteetColumnDataProps = {
  model: SuoritusModel
  ylioppilastutkinto: SuoritusModel
}

export const KoepisteetColumn: KoepisteetColumn = {
  shouldShow: ({ parentSuoritus }) => isYlioppilastutkinto(parentSuoritus),
  renderHeader: () => {
    return (
      <th key="koepisteet" className="koepisteet" scope="col">
        {/* @ts-expect-error */}
        <Text name="Pisteet" />
      </th>
    )
  },
  renderData: ({ model, ylioppilastutkinto }) => (
    <td
      key="koepisteet"
      className={`koepisteet ${ylioppilastutkinto ? 'ylioppilas' : ''}`}
    >
      <Editor model={modelLookup(model, 'arviointi.-1.pisteet')} />
    </td>
  )
}

// LaajuusColumn

export type LaajuusColumn = ColumnIface<
  LaajuusColumnDataProps,
  LaajuusColumnShowProps,
  LaajuusColumnHeaderProps
>

export type LaajuusColumnShowProps = {
  parentSuoritus: SuoritusModel
  suoritukset: SuoritusModel[]
  suorituksetModel: SuoritusModel
  context: BaseContext
}

export type LaajuusColumnHeaderProps = {
  laajuusYksikkö?: string
}

export type LaajuusColumnDataProps = {
  model: SuoritusModel
  showScope?: boolean
}

export const LaajuusColumn: LaajuusColumn = {
  shouldShow: ({ parentSuoritus, suoritukset, suorituksetModel, context }) => {
    if (isNäyttötutkintoonValmistava(parentSuoritus)) {
      return false
    } else if (
      isVapaanSivistystyönOppivelvollistenSuoritus(parentSuoritus) &&
      context.edit
    ) {
      return false
    } else if (isEshPäätasonSuoritus(parentSuoritus)) {
      return true
    } else if (isEshOsasuoritus(parentSuoritus)) {
      return false
    } else {
      return context.edit
        ? modelProperty(
            createTutkinnonOsanSuoritusPrototype(suorituksetModel),
            'koulutusmoduuli.laajuus'
          ) !== null
        : suoritukset.find(
            (s) => modelData(s, 'koulutusmoduuli.laajuus.arvo') !== undefined
          ) !== undefined
    }
  },
  renderHeader: ({ laajuusYksikkö }) => {
    return (
      <th key="laajuus" className="laajuus" scope="col">
        {/* @ts-expect-error */}
        <Text name="Laajuus" />
        {(laajuusYksikkö && ' (' + laajuusYksikkö + ')') || ''}
      </th>
    )
  },
  renderData: ({ model, showScope }) => (
    <td key="laajuus" className="laajuus">
      <Editor
        model={model}
        path="koulutusmoduuli.laajuus"
        compact="true"
        showReadonlyScope={showScope}
      />
    </td>
  )
}

// ArvosanaColumn

export type ArvosanaColumn = ColumnIface<
  ArvosanaColumnDataProps,
  ArvosanaColumnShowProps
>

export type ArvosanaColumnShowProps = {
  parentSuoritus: SuoritusModel
  suoritukset: SuoritusModel[]
  context: BaseContext
}

export type ArvosanaColumnDataProps = {
  model: SuoritusModel
  ylioppilastutkinto?: SuoritusModel
}

export const ArvosanaColumn: ColumnIface<
  ArvosanaColumnDataProps,
  ArvosanaColumnShowProps
> = {
  shouldShow: ({ parentSuoritus, suoritukset, context }) =>
    !isNäyttötutkintoonValmistava(parentSuoritus) &&
    !isVapaanSivistystyönOppivelvollistenSuoritus(parentSuoritus) &&
    !isEshS7(parentSuoritus) &&
    (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => (
    <th key="arvosana" className="arvosana" scope="col">
      {/* @ts-expect-error */}
      <Text name="Arvosana" />
    </th>
  ),
  renderData: ({ model, ylioppilastutkinto }) => (
    <td
      key="arvosana"
      className={`arvosana ${ylioppilastutkinto ? 'ylioppilas' : ''}`}
    >
      {/* @ts-expect-error */}
      <ArvosanaEditor model={model} />
    </td>
  )
}

// TaitotasoColmn

export type TaitotasoColumn = ColumnIface<
  TaitotasoColmnDataProps,
  TaitotasoColmnShowProps
>

export type TaitotasoColmnShowProps = {
  parentSuoritus: SuoritusModel
}

export type TaitotasoColmnDataProps = {
  model: SuoritusModel
}

export const TaitotasoColumn: TaitotasoColumn = {
  shouldShow: ({ parentSuoritus }) =>
    isLukutaitokoulutuksenSuoritus(parentSuoritus),
  renderHeader: () => (
    <th key="taitotaso">
      {/* @ts-expect-error */}
      <Text name={'Taitotaso'} />
    </th>
  ),
  renderData: ({ model }) => (
    <td key="taitotaso" className={'taitotaso'}>
      <Editor model={model} path={'arviointi.-1.taitotaso'} />
    </td>
  )
}
