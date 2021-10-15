package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation

object RaportointiDatabaseCustomFunctions {
  def vuodenViimeinenPäivämäärä(s: Schema) = {
    sqlu"""
      create or replace function #${s.name}.vuodenViimeinenPaivamaara(timestamp) returns date
        as 'select to_date(concat(
              extract(year from ($$1)::date)::text,
              ''-12-31''
            ),
          ''YYYY-MM-DD'')'
      language sql
      immutable
      returns null on null input;
      """
  }

  def vuodenEnsimmäinenPäivämäärä(s: Schema) = {
    sqlu"""
      create or replace function #${s.name}.vuodenEnsimmainenPaivamaara(timestamp) returns date
        as 'select to_date(concat(
              extract(year from ($$1)::date)::text,
              ''-01-01''
            ),
          ''YYYY-MM-DD'')'
      language sql
      immutable
      returns null on null input;
      """
  }
}
