import bem from "bem-ts"
import React from "react"
import { Link } from "react-router-dom"
import { fetchHenkilöhaku, fetchHenkilöhakuCache } from "../../api/api"
import { useApiMethod } from "../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../api/apiUtils"
import { SubmitButton } from "../../components/buttons/SubmitButton"
import { Form } from "../../components/forms/Form"
import { TextField } from "../../components/forms/TextField"
import { Spinner } from "../../components/icons/Spinner"
import { Error } from "../../components/typography/error"
import { NoDataMessage } from "../../components/typography/NoDataMessage"
import { T, t } from "../../i18n/i18n"
import { HenkilöHakutiedot } from "../../state/apitypes/henkilo"
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

export type OppijaSearchProps = {}

export const OppijaSearch = (_props: OppijaSearchProps) => {
  const form = useFormState({ initialValues, validators })
  const search = useApiMethod(fetchHenkilöhaku, fetchHenkilöhakuCache)

  const submit = form.submitCallback((data) => {
    search.call(data.query)
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
          disabled={!form.isValid || isLoading(search)}
          value={t("oppijahaku__hae")}
        />
        <div className={b("results")}>
          {isLoading(search) && <Spinner />}
          {isSuccess(search) && <OppijaSearchResults henkilö={search.data} />}
          {isError(search) &&
            (search.status === 403 ? (
              <NoDataMessage>
                <T id="oppijahaku__ei_tuloksia" />
              </NoDataMessage>
            ) : (
              <Error>
                {search.errors.map((error) => error.message).join("; ")}
              </Error>
            ))}
        </div>
      </TextField>
    </Form>
  )
}

export type OppijaSearchResultsProps = {
  henkilö: HenkilöHakutiedot
}

const OppijaSearchResults = (props: OppijaSearchResultsProps) => {
  const basePath = useBasePath()
  const result = props.henkilö

  return (
    <div>
      <T id="oppijahaku__löytyi" />
      {": "}
      <Link to={createOppijaPath(basePath, { oppijaOid: result.oid })}>
        {result.sukunimi} {result.etunimet} {result.hetu && `(${result.hetu})`}
      </Link>
    </div>
  )
}
