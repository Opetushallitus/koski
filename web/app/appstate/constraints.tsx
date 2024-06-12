import * as E from 'fp-ts/Either'
import * as C from '../util/constraints'
import { pipe } from 'fp-ts/lib/function'
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { fetchConstraint } from '../util/koskiApi'
import { ClassOf, ObjWithClass, schemaClassName } from '../util/types'

/*
 * CONSTRAINTS
 *
 * Constraint on tietomallia tai sen osaa kuvaava skeema. Siinä on piirteitä niin vanhasta editormodelista kuin myös json schemasta.
 * Ne muodostavat puurakenteen, jossa jokainen solmu on tyypitetty. Eri tyypit löytyvät kansiosta /web/app/types/fi/oph/koski/typemodel
 *
 * Tyypillisiä käyttötapauksia on hakea kenttään sallittujen arvojen rajoitukset (esim. ylä- tai alaraja numeroarvolle),
 * sallitut koodiarvot tai tietyn luokan lapsiluokkien nimet.
 *
 * Kaikki allaolevat hookit hyväksyvät luokan nimen lyhyessä ja pitkässä muodossa, esim. "Vahvistus" ja "fi.oph.koski.schema.Vahvistus"
 * ovat molemmat oikein. Haku on rajoitettu vain fi.oph.koski.schema-pakettiin. Vinkki: Tietomallin luokkien nimet löytyvät myös
 * exportatun tyypityksen konstrukorifunktioista:
 *
 *    import { Vahvistus } from '../types/fi/oph/koski/schema/Vahvistus'
 *
 *    // ...
 *
 *    const vahvistus = useSchema(Vahvistus.className)
 *
 * Tiedostosta /web/app/util/constraints.ts löytyy erilaisia apufunktioita constraintien käsittelyyn, jota on suunniteltu koostettavaksi
 * fp-ts-kirjaston pipe-funktion kanssa. Esimerkiksi opiskeluoikeuden päätason suorituksen luokan nimen saa haettua näin:
 *
 *    import * as C from '../util/constraints'
 *    import { pipe } from 'fp-ts'
 *
 *    // ...
 *
 *    const opiskeluoikeus = useSchema(opiskeluoikeusClassName)
 *    const suoritusClassName = pipe(
 *      opiskeluoikeus,
 *      C.path('suoritukset.[]'),
 *      C.className<Suoritus>()
 *    )
 */

/**
 * Palauttaa luokan nimeä vastaavan constraintin.
 *
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @returns Constraint jos luokka löytyi ja lataaminen onnistui, null jos haku kesken tai tietojen lataaminen epäonnistui.
 */
export const useSchema = (className?: string | null): Constraint | null => {
  const { constraints, loadConstraint } = useContext(ConstraintsContext)
  const schemaClass = useMemo(
    () => (className && schemaClassName(className)) || className,
    [className]
  )
  useEffect(() => {
    if (schemaClass) {
      loadConstraint(schemaClass)
    }
  }, [loadConstraint, schemaClass])
  const constraint = schemaClass && constraints[schemaClass]
  return typeof constraint === 'object' ? constraint : null
}

/**
 * Palauttaa luokan nimen ja polun osoittaman constraintin.
 *
 * Heittää poikkeuksen, jos:
 *    - polun osoittamaa kenttää ei löydy
 *
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @param path merkkijonoon osoittava polku, esim. "tila.opiskeluoikeusjaksot.[].tila.koodiarvo"
 * @returns Constraint[] jos luokka löytyi ja lataaminen onnistui, null jos haku kesken tai tietojen lataaminen epäonnistui.
 */
export const useChildSchema = (
  className: string | null | undefined,
  path: string
): Constraint | null => {
  const c = useSchema(className)
  return useMemo(
    () => pipe(c, C.asList, C.path(path), (cs) => cs?.[0] || null),
    [c, path]
  )
}

/**
 * Palauttaa luokan nimen ja polun osoittaman merkkijonon (tavallisesti koodiviitteen uri tai koodiarvo) sallitut arvot.
 * Jos arvoja ei ole rajattu, palautettu lista on tyhjä.
 *
 * Heittää poikkeuksen, jos:
 *    - polun osoittamaa kenttää ei löydy
 *    - tai polun osoittamalla kenttä ei ole merkkijono.
 *
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @param path merkkijonoon osoittava polku, esim. "tila.opiskeluoikeusjaksot.[].tila.koodiarvo"
 * @returns string[] jos kenttä löytyi, null jos haku kesken, tietojen lataaminen epäonnistui
 */
export const useAllowedStrings = (
  className: string | null | undefined,
  path: string
): string[] | null => {
  const c = useSchema(className)
  return useMemo(
    () => pipe(c, C.asList, C.path(path), C.allAllowedStrings),
    [c, path]
  )
}

/**
 * Palauttaa luokan ja polun osoittaman lapsiluokan nimen.
 *
 * Heittää poikkeuksen, jos:
 *    - polun osoittamaa kenttää ei löydy
 *    - tai polun osoittamalla kenttä ei ole luokka
 *    - tai mahdollisia luokan nimiä on useampi kuin yksi.
 *
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @param path merkkijonoon osoittava polku, esim. "suoritukset.[].tyyppi.koodiarvo"
 * @returns string jos kenttä löytyi, null jos haku kesken, tietojen lataaminen epäonnistui
 */
export const useChildClassName = <T extends ObjWithClass>(
  className: string | null | undefined,
  path: string
): ClassOf<T> | null => {
  const c = useSchema(className)
  return useMemo(
    () => pipe(c, C.asList, C.path(path), C.classNames<T>(), C.singular),
    [c, path]
  )
}

/**
 * Palauttaa luokan ja polun osoittamien lapsiluokkien nimet.
 *
 * Heittää poikkeuksen, jos:
 *    - polun osoittamaa kenttää ei löydy
 *    - tai polun osoittamalla kenttä ei ole luokka.
 *
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @param path merkkijonoon osoittava polku, esim. "suoritukset.[].tyyppi.koodiarvo"
 * @returns string[] jos kenttä löytyi, null jos haku kesken, tietojen lataaminen epäonnistui
 */
export const useChildClassNames = <T extends ObjWithClass>(
  className: string | null | undefined,
  path: string
): ClassOf<T>[] | null => {
  const c = useSchema(className)
  return useMemo(
    () => pipe(c, C.asList, C.path(path), C.classNames<T>()),
    [c, path]
  )
}

// Context

const Loading = Symbol('loading')

export type ConstraintsRecord = Record<string, Constraint | typeof Loading>

export type ConstraintsContextValue = {
  readonly constraints: ConstraintsRecord
  readonly loadConstraint: (schemaClass: string) => void
}

const ConstraintsContext = React.createContext<ConstraintsContextValue>({
  constraints: {},
  loadConstraint: () => {}
})

export type ConstraintsProviderProps = React.PropsWithChildren<{}>

class ConstraintsLoader {
  constraints: ConstraintsRecord = {}

  async loadConstraint(schemaClass: string): Promise<boolean> {
    if (!this.constraints[schemaClass]) {
      this.constraints[schemaClass] = Loading

      pipe(
        await fetchConstraint(schemaClass),
        E.map((response) => {
          this.constraints = {
            ...this.constraints,
            [schemaClass]: response.data
          }
        })
      )
      return true
    }
    return false
  }
}

const constraintsLoaderSingleton = new ConstraintsLoader()

export const ConstraintsProvider = (props: ConstraintsProviderProps) => {
  const [constraints, setConstraints] = useState<ConstraintsRecord>({})

  const loadConstraint = useCallback(async (schemaClass: string) => {
    if (await constraintsLoaderSingleton.loadConstraint(schemaClass)) {
      setConstraints(constraintsLoaderSingleton.constraints)
    }
  }, [])

  const providedValue: ConstraintsContextValue = useMemo(
    () => ({ constraints, loadConstraint }),
    [constraints, loadConstraint]
  )

  return (
    <ConstraintsContext.Provider value={providedValue}>
      {props.children}
    </ConstraintsContext.Provider>
  )
}
