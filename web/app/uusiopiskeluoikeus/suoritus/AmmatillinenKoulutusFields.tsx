import React, { useCallback, useMemo, useState } from 'react'
import { isSuccess, useApiWithParams } from '../../api-fetch'
import { useKoodisto } from '../../appstate/koodisto'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { TextEdit } from '../../components-v2/controls/TextField'
import { t } from '../../i18n/i18n'
import { isAmmatilliseenTehtäväänValmistavaKoulutus } from '../../types/fi/oph/koski/schema/AmmatilliseenTehtavaanValmistavaKoulutus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { TutkintoPeruste } from '../../types/fi/oph/koski/tutkinto/TutkintoPeruste'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { fetchOppilaitoksenPerusteet } from '../../util/koskiApi'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import {
  createAmmatilliseenTehtäväänValmistavaKoulutus,
  createPaikallinenMuuAmmatillinenKoulutus,
  createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
} from '../opintooikeus/ammatillinenTutkinto'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { SuoritusFieldsProps } from './SuoritusFields'

export const AmmatillinenKoulutusFields = (props: SuoritusFieldsProps) => {
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
      <label>
        {t('Suoritustyyppi')}
        <DialogPäätasonSuoritusSelect
          state={props.state}
          testId="suoritustyyppi"
        />
      </label>

      {props.state.suoritustapa.visible && (
        <label>
          {t('Suoritustapa')}
          <DialogKoodistoSelect
            state={props.state.suoritustapa}
            koodistoUri="ammatillisentutkinnonsuoritustapa"
            testId="suoritustapa"
          />
        </label>
      )}

      {props.state.muuAmmatillinenKoulutus.visible && (
        <MuuAmmatillinenKoulutusFields {...props} />
      )}

      {props.state.tutkinto.visible && (
        <label>
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
        </label>
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
      <label>
        {t('Koulutusmoduuli')}
        <Select
          autoselect
          options={muuAmmatillinenKoulutusOptions}
          value={koulutusmoduuli}
          onChange={(opt) => setKoulutusmoduuli(opt?.value)}
          testId="koulutusmoduuli"
        />
      </label>

      {koulutusmoduuli === 'paikallinen' && (
        <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
      )}

      {koulutusmoduuli === 'ammatilliseentehtavaanvalmistavakoulutus' && (
        <label>
          {t('Ammatilliseen tehtävään valmistava koulutus')}
          <Select
            autoselect
            options={tunnisteOptions}
            value={tunniste}
            onChange={onAmmatilliseenTehtäväänValmistavaKoulutus}
            testId="ammatilliseentehtavaanvalmistavakoulutus"
          />
        </label>
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
    <section className="PaikallinenKoulutus">
      <label>
        {t('Nimi')}
        <TextEdit value={koulutus.nimi} onChange={update('nimi')} />
      </label>
      <label>
        {t('Koodiarvo')}
        <TextEdit value={koulutus.koodiarvo} onChange={update('koodiarvo')} />
      </label>
      <label>
        {t('Kuvaus')}
        <TextEdit value={koulutus.kuvaus} onChange={update('kuvaus')} />
      </label>
    </section>
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
