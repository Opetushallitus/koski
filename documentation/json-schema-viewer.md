# JSON Schema Viewer (vendored, Koski-patched)

The interactive JSON Schema viewer served at `/koski/json-schema-viewer/`
(rendered by `JsonSchemaViewerHtmlServlet`). It powers the schema browsers linked
from the API documentation, including the omadata packages. The vendored assets
live in `web/static/json-schema-viewer/`.

## Provenance

This is a vendored copy of the **`dist` output** (v0.3.4) of
[Opetushallitus/json-schema-viewer](https://github.com/Opetushallitus/json-schema-viewer),
a fork of the upstream [ADIwg/json-schema-viewer](https://github.com/adiwg/json-schema-viewer)
("mdJSON" viewer).

**The upstream fork is effectively dormant** — its `master` has not moved since
2016 (only automated dependency-bump branches since). Only the built artifacts
were imported; the fork's actual source (`json-schema-viewer.js`, LESS, Handlebars
templates, Gruntfile) is **not** in this repo. In practice we own and maintain
this copy directly.

## Koski-local patches (NOT present upstream)

These are applied directly to the vendored `js`/`styles` files in
`web/static/json-schema-viewer/`. A future re-vendor from upstream **must
re-apply them**:

- **`redundantData`** — highlights fields tagged `@RedundantData`.
- **`sensitive`** (TOR-2621) — highlights fields tagged `@SensitiveData`: node
  flag propagation, `.classed("sensitive")`, legend entry, and darksalmon tree
  text (`fill`).
- **Lifecycle de-emphasis** — deprecated/redundant fields render dimmed to gray
  (`fill: #777`) with a strikethrough, instead of the previous orange. Colour
  and strikethrough are independent channels, so a field that is both sensitive
  and deprecated/redundant keeps its darksalmon colour and gains the strike
  (both signals; sensitive wins the `fill`). Note: on SVG text the strike line
  follows `fill` and ignores `text-decoration-color`. The two lifecycle legend
  rows are merged into one "Deprecated / unused".
- **Info-panel clause spacing** — each appended annotation clause (`(Oksa: …)`,
  `(Vanhentunut kenttä: …)`, sensitive label, …) is shown on its own line.
- **Info-panel title readability** — the title is not given field styling, the
  inherited jQuery Mobile letterpress `text-shadow` is removed (it blurred the
  tree/info text), and the title shows long property names in full (`margin: 0`,
  `overflow: visible`, `white-space: normal`).
- **Selected-node highlight** — each node gets a `.focus-box` `<rect>` (sized
  from the label length, since the tree font is monospace) shown only on the
  focused node as a rounded green outline around the label. Replaces the plain
  bold, which was hard to see.
- **Info-panel Definition layout** (TOR-2464) — the info panel's Definition tab
  is restructured (design direction "1a") into two sections: a **Technical**
  definition-list table and a **Description** section. The Technical table emits
  a row only when the fact is present, read from the schema's **structured** JSON
  fields — `Type`, `Cardinality` (array/object only, from `minItems`/`maxItems`/
  required), `Minimum`/`Maximum`, `Format` (`pattern`), `Allowed` (enum chips),
  and an `Annotation` row of mono chips (`@SensitiveData`/`@RedundantData`/
  `@Deprecated`). Behaviour-changing annotations also render an icon **badge**
  (lock for sensitive, circle-slash for "not in use"). The Description section
  shows one block per language (`fi`/`sv`/`en`: tag + bold term + prose) from
  `node.translation`. The panel never parses the `description` string. The dark
  header + tab row are styled over jQuery Mobile; the tab row is kept as the
  current product. See "Localized definition panel" below.

The `deprecated` / `redundantData` / `sensitive` booleans the viewer reads come
from the schema JSON, emitted by the matching annotations in
`src/main/scala/fi/oph/koski/schema/annotation/` (`Deprecated`, `RedundantData`,
`Annotations.scala` → `SensitiveData`).

## Localized definition panel (TOR-2464)

The info panel shows each schema node's title and definition in fi/sv/en,
populated server-side. Each viewer schema is served through `LocalizedSchemas`
(`fi.oph.koski.documentation`), which:

- builds the same `ClassSchema` as the corresponding `*Schema` object
  (`KoskiSchema.createSchema`),
- attaches a `translation` field to each property and class node via
  `SchemaLocalizationEnricher` (`fi.oph.koski.localization`), a
  `SchemaJsonDecorator` (TOR-2646) that scala-schema calls during its single
  `SchemaToJson.toJsonSchema` traversal. Content is resolved from
  `koskiLocalizationRepository` using the same keys as
  `KoskiSpecificSchemaLocalization` — **title + description only**. Class nodes
  use the class title as the key, so class/object titles are translated too.
  Because the decorator sees every emitted node, it also translates the
  specialized koodisto definitions (e.g. `Koodistokoodiviite[opiskeluoikeudentyyppi]`)
  that the previous by-`simpleName` re-walk silently skipped,
- caches the enriched JSON ~1 min (matching the localization cache refresh),
  so translation edits appear without a restart.

The emitted shape is language-keyed and includes Finnish (shown as its own
`fi` block):

```json
"translation": {
  "fi": { "title": "…", "description": "…" },
  "sv": { "title": "…", "description": "…" },
  "en": { … }
}
```

Notes:

- Tooltip/info-link annotations aren't emitted into the schema JSON, so they
  aren't shown; missing keys/languages are skipped, and a node with no
  translation gets no `translation` field.
- The `translation` keyword is non-standard and ignored by JSON Schema
  validators, so it is a safe additive change to the documentation schemas
  (which external integrators may also consume).
- The per-language **prose** renders markdown links `[teksti](url)` in the
  description as real anchors (client-side, in `renderProse`). Only `http`/`https`
  URLs are linkified and the text is HTML-escaped first, so other schemes and
  markup are inert. The bold **term** is still plain text.
- The technical block reads only **structured** JSON fields, never the
  `description` string. Several Koski annotations were given structured fields so
  they can be shown as their own row/chip: `KoodistoUri` → `koodisto` (Koodisto
  row, linked), `KoodistoKoodiarvo` → `koodiarvot` (Allowed row), `OksaUri` →
  `oksa` (Oksa row, linked), `Deprecated` → `deprecatedMessage` (badge),
  `UnitOfMeasure` → `unit` (Format row), `ReadOnly` → `readOnly`/`readOnlyText`
  (Read-only row). These are additive keywords, ignored by JSON Schema validators.
- Still **not** shown, because it only lives in the `description` string:
  `@DefaultValue` (it's a scala-schema annotation, not Koski).
- To cover a new schema, register it in `LocalizedSchemas`.

## Editing / build

- `json-schema-viewer.js` is **pretty-printed source** — edit it directly.
  Terser minifies it during the frontend build.
- `json-schema-viewer.css` is **un-minified source** — edit it directly. It is
  served verbatim (webpack does not minify copied static CSS); a ~15 KB dev-tool
  stylesheet, so size is not a concern.
- Assets are served from `target/webapp/`, produced by webpack's
  `CopyWebpackPlugin` copying `web/static/`. **After editing, run `make front`**
  to regenerate `target/webapp`, then hard-refresh the browser — editing
  `web/static` alone has no effect on the running app until rebuilt.
- Verify what is actually served (bypassing browser cache) with e.g.
  `curl -s http://localhost:7021/koski/json-schema-viewer/styles/json-schema-viewer.css`.
