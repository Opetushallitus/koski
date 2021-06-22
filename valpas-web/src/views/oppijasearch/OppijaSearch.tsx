import bem from "bem-ts"
import React from "react"
import { Link } from "react-router-dom"
import { ApiMethodHook } from "../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../api/apiUtils"
import { SubmitButton } from "../../components/buttons/SubmitButton"
import { Form } from "../../components/forms/Form"
import { TextField } from "../../components/forms/TextField"
import { Spinner } from "../../components/icons/Spinner"
import { T, t } from "../../i18n/i18n"
import {
  HenkilöhakuResult,
  isLöytyiHenkilöhakuResult,
  LöytyiHenkilöhakuResult,
} from "../../state/apitypes/henkilohaku"
import { useBasePath } from "../../state/basePath"
import {
  expectAtLeastOne,
  expectValidHetu,
  expectValidOid,
} from "../../state/formValidators"
import { createOppijaPath } from "../../state/paths"
import { FormValidators, useFormState } from "../../state/useFormState"
import "./OppijaSearch.less"

const b = bem("oppijasearch")

type OppijaSearchValues = {
  query: string
}

const initialValues: OppijaSearchValues = {
  query: "",
}

const validators: FormValidators<OppijaSearchValues> = {
  query: [
    expectAtLeastOne("oppijahaku__validointivirhe", [
      expectValidHetu(),
      expectValidOid(),
    ]),
  ],
}

export type OppijaSearchProps = {
  searchApiMethod: ApiMethodHook<HenkilöhakuResult, [query: string]>
  prevPath: string
  eiLöytynytIlmoitusId: string
  error403Id: string
}

export const OppijaSearch = (props: OppijaSearchProps) => {
  const form = useFormState({ initialValues, validators })

  const submit = form.submitCallback((data) => {
    props.searchApiMethod.call(data.query)
  })

  return (
    <Form className={b()} onSubmit={submit}>
      <TextField
        label={t("oppijahaku__hae_hetulla_tai_oppijanumerolla")}
        {...form.fieldProps("query")}
      >
        <SubmitButton
          className={b("submit")}
          onClick={form.submitCallback(console.log)}
          disabled={!form.isValid || isLoading(props.searchApiMethod)}
          value={t("oppijahaku__hae")}
        />
        <div className={b("results")}>
          {isLoading(props.searchApiMethod) && <Spinner />}
          {isSuccess(props.searchApiMethod) && (
            <OppijaSearchResults
              hakutulos={props.searchApiMethod.data}
              eiLöytynytIlmoitusId={props.eiLöytynytIlmoitusId}
              prevPath={props.prevPath}
            />
          )}
          {isError(props.searchApiMethod) && (
            <OppijaSearchError
              statusCode={props.searchApiMethod.status}
              error403Id={props.error403Id}
            />
          )}
        </div>
      </TextField>
    </Form>
  )
}

type OppijaSearchResultsProps = {
  hakutulos: HenkilöhakuResult
  eiLöytynytIlmoitusId: string
  prevPath: string
}

const OppijaSearchResults = (props: OppijaSearchResultsProps) => {
  if (isLöytyiHenkilöhakuResult(props.hakutulos)) {
    return (
      <OppijaSearchMatchResult
        henkilö={props.hakutulos}
        prevPath={props.prevPath}
      />
    )
  }
  return (
    <OppijaSearchUndefinedResult
      eiLöytynytIlmoitusId={props.eiLöytynytIlmoitusId}
    />
  )
}

type OppijaSearchUndefinedResult = {
  eiLöytynytIlmoitusId: string
}

const OppijaSearchUndefinedResult = (props: OppijaSearchUndefinedResult) => (
  <div className={b("resultvalue")}>
    <T id={props.eiLöytynytIlmoitusId} />
  </div>
)

type OppijaSearchMatchResultProps = {
  henkilö: LöytyiHenkilöhakuResult
  prevPath: string
}

const OppijaSearchMatchResult = (props: OppijaSearchMatchResultProps) => {
  const basePath = useBasePath()
  const result = props.henkilö

  return (
    <div className={b("resultvalue")}>
      <T id="oppijahaku__löytyi" />
      {": "}
      <Link
        className={b("resultlink")}
        to={createOppijaPath(basePath, {
          oppijaOid: result.oid,
          prev: props.prevPath,
        })}
      >
        {result.sukunimi} {result.etunimet} {result.hetu && `(${result.hetu})`}
      </Link>
    </div>
  )
}

type OppijaSearchErrorProps = {
  statusCode?: number
  error403Id: string
}

const OppijaSearchError = (props: OppijaSearchErrorProps) => {
  const [message, showAsError] = getErrorText(
    props.error403Id,
    props.statusCode
  )
  return (
    <div className={b("resultvalue", { error: showAsError })}>{message}</div>
  )
}

const getErrorText = (
  error403Id: string,
  statusCode?: number
): [string, boolean] => {
  switch (statusCode) {
    case 400:
      return [t("oppijahaku__validointivirhe"), true]
    case 403:
      return [t(error403Id), false]
    default:
      return [t("apivirhe__virheellinen_pyyntö", { virhe: status }), true]
  }
}
