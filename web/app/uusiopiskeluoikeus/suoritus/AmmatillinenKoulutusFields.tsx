import React, { useCallback, useMemo, useState, useEffect } from 'react'
import { isSuccess, useApiWithParams } from '../../api-fetch'
import { useKoodisto } from '../../appstate/koodisto'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { TextEdit } from '../../components-v2/controls/TextField'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { fetchOppilaitoksenPerusteet } from '../../util/koskiApi'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import {
  createAmmatilliseenTehtäväänValmistavaKoulutus,
  createPaikallinenMuuAmmatillinenKoulutus,
  createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
} from '../opintooikeus/createAmmatillinenTutkintoOpiskeluoikeus'
import { usePäätasonSuoritustyypit } from '../state/hooks'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { SuoritusFieldsProps } from './SuoritusFields'
import { isAmmatilliseenTehtäväänValmistavaKoulutus } from '../../types/fi/oph/koski/schema/AmmatilliseenTehtavaanValmistavaKoulutus'

export const AmmatillinenKoulutusFields = (props: SuoritusFieldsProps) => {
  const suoritusOptions = usePäätasonSuoritustyypit(props.state)
  const tutkinnot = useTutkinnot(props.state)

  const onTOPKS = useCallback(
    (koulutus?: PaikallinenKoulutus) =>
      props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus.set(
        koulutus &&
          createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus(
            koulutus.nimi,
            koulutus.koodiarvo,
            koulutus.kuvaus
          )
      ),
    [props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus]
  )

  return (
    <>
      {t('Suoritustyyppi')}
      <Select
        options={suoritusOptions}
        value={
          props.state.päätasonSuoritus.value &&
          koodistokoodiviiteId(props.state.päätasonSuoritus.value)
        }
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="suoritustyyppi"
      />

      {props.state.suoritustapa.visible && (
        <>
          {t('Suoritustapa')}
          <DialogKoodistoSelect
            state={props.state.suoritustapa}
            koodistoUri="ammatillisentutkinnonsuoritustapa"
            testId="suoritustapa"
          />
        </>
      )}

      {props.state.muuAmmatillinenKoulutus.visible && (
        <MuuAmmatillinenKoulutusFields {...props} />
      )}

      {props.state.tutkinto.visible && (
        <>
          {t('Tutkinto')}
          <Select
            options={tutkinnot.options}
            value={
              props.state.tutkinto.value &&
              tutkintoKey(props.state.tutkinto.value)
            }
            onChange={(opt) => props.state.tutkinto.set(opt?.value)}
            onSearch={tutkinnot.setQuery}
            testId="tutkinto"
          />
        </>
      )}

      {props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
        .visible && <PaikallinenKoulutusFields onChange={onTOPKS} />}

      {props.state.peruste.visible && (
        <DialogPerusteSelect state={props.state} />
      )}
    </>
  )
}

const useTutkinnot = (state: UusiOpiskeluoikeusDialogState) => {
  const [query, setQuery] = useState<string>()

  const tutkinnot = useApiWithParams(
    fetchOppilaitoksenPerusteet,
    state.oppilaitos.value !== undefined
      ? [state.oppilaitos.value?.oid, query]
      : undefined
  )

  const options = useMemo(
    () =>
      isSuccess(tutkinnot)
        ? tutkinnot.data
            .map(
              (k) =>
                ({
                  key: tutkintoKey(k),
                  value: k,
                  label: `${k.tutkintoKoodi} ${t(k.nimi)} (${k.diaarinumero})`
                }) satisfies SelectOption<TutkintoPeruste>
            )
            .sort((a, b) => a.key.localeCompare(b.key))
        : [],
    [tutkinnot]
  )

  return { options, setQuery }
}

const tutkintoKey = (tutkinto: TutkintoPeruste): string =>
  `${tutkinto.tutkintoKoodi}_${tutkinto.diaarinumero}`

const MuuAmmatillinenKoulutusFields = (props: SuoritusFieldsProps) => {
  const [koulutusmoduuli, setKoulutusmoduuli] =
    useState<MuuAmmatillinenKoulutusmoduuliKey>()

  const tunnisteet = useKoodisto('ammatilliseentehtavaanvalmistavakoulutus')
  const tunnisteOptions = useMemo(
    () => (tunnisteet ? groupKoodistoToOptions(tunnisteet) : []),
    [tunnisteet]
  )

  const onPaikallinenKoulutus = useCallback(
    (koulutus?: PaikallinenKoulutus) => {
      props.state.muuAmmatillinenKoulutus.set(
        koulutus &&
          createPaikallinenMuuAmmatillinenKoulutus(
            koulutus.nimi,
            koulutus.koodiarvo,
            koulutus.kuvaus
          )
      )
    },
    [props.state.muuAmmatillinenKoulutus]
  )

  const onAmmatilliseenTehtäväänValmistavaKoulutus = useCallback(
    (
      option?: SelectOption<
        Koodistokoodiviite<'ammatilliseentehtavaanvalmistavakoulutus'>
      >
    ) =>
      props.state.muuAmmatillinenKoulutus.set(
        option?.value &&
          createAmmatilliseenTehtäväänValmistavaKoulutus(option.value)
      ),
    [props.state.muuAmmatillinenKoulutus]
  )

  const tunniste = isAmmatilliseenTehtäväänValmistavaKoulutus(
    props.state.muuAmmatillinenKoulutus.value
  )
    ? koodistokoodiviiteId(props.state.muuAmmatillinenKoulutus.value.tunniste)
    : undefined

  return (
    <>
      {t('Koulutusmoduuli')}
      <Select
        options={muuAmmatillinenKoulutusOptions}
        value={koulutusmoduuli}
        onChange={(opt) => setKoulutusmoduuli(opt?.value)}
        testId="koulutusmoduuli"
      />

      {koulutusmoduuli === 'paikallinen' && (
        <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
      )}

      {koulutusmoduuli === 'ammatilliseentehtavaanvalmistavakoulutus' && (
        <>
          {t('Ammatilliseen tehtävään valmistava koulutus')}
          <Select
            options={tunnisteOptions}
            value={tunniste}
            onChange={onAmmatilliseenTehtäväänValmistavaKoulutus}
            testId="ammatilliseentehtavaanvalmistavakoulutus"
          />
        </>
      )}
    </>
  )
}

type PaikallinenKoulutusFieldsProps = {
  onChange: (values?: PaikallinenKoulutus) => void
}

type PaikallinenKoulutus = {
  nimi: string
  koodiarvo: string
  kuvaus: string
}

const emptyPaikallinenKoulutus: PaikallinenKoulutus = {
  nimi: '',
  koodiarvo: '',
  kuvaus: ''
}

const PaikallinenKoulutusFields = (props: PaikallinenKoulutusFieldsProps) => {
  const [koulutus, setKoulutus] = useState<PaikallinenKoulutus>(
    emptyPaikallinenKoulutus
  )

  const update = (field: keyof PaikallinenKoulutus) => (value?: string) => {
    const patched: PaikallinenKoulutus = { ...koulutus, [field]: value }
    setKoulutus(patched)
    props.onChange(
      patched.nimi && patched.koodiarvo && patched.kuvaus ? patched : undefined
    )
  }

  return (
    <>
      {t('Nimi')}
      <TextEdit value={koulutus.nimi} onChange={update('nimi')} />
      {t('Koodiarvo')}
      <TextEdit value={koulutus.koodiarvo} onChange={update('koodiarvo')} />
      {t('Kuvaus')}
      <TextEdit value={koulutus.kuvaus} onChange={update('kuvaus')} />
    </>
  )
}

type MuuAmmatillinenKoulutusmoduuliKey =
  | 'paikallinen'
  | 'ammatilliseentehtavaanvalmistavakoulutus'

const muuAmmatillinenKoulutusOptions: SelectOption<MuuAmmatillinenKoulutusmoduuliKey>[] =
  [
    {
      key: 'paikallinen',
      value: 'paikallinen',
      label: t('Paikallinen koulutus')
    },
    {
      key: 'ammatilliseentehtavaanvalmistavakoulutus',
      value: 'ammatilliseentehtavaanvalmistavakoulutus',
      label: t('Ammatilliseen tehtävään valmistava koulutus')
    }
  ]
