const { apply_patch } = require("jsonpatch");
const { writeFileSync, existsSync, readFileSync } = require("fs");

/**
 * @typedef {{ op: "add" | "remove" | "replace" | "move" | "copy" | "test", path: string, from?: string, value?: unknown }} Patch
 * @typedef {{ versionumero: number, muutos: Patch[] }} HistoryEntry
 */

const run = async () => {
  const file = readFileSync("historia.jsons");
  /** @type {HistoryEntry[]} */
  const history = file
    .toString()
    .split("\n")
    .filter((s) => !!s)
    .map((line) => JSON.parse(line));

  history.sort((a, b) => a.versionumero - b.versionumero);

  history.reduce((/** @type {unknown} */ data, patch) => {
    const hotfix = `hotfix-${patch.versionumero}.json`;
    /** @type {Patch[]} */
    const patchOrHotfix = existsSync(hotfix)
      ? (console.log(`Yliajetaan hotfix patchille ${patch.versionumero}`),
        JSON.parse(readFileSync(hotfix, "utf8")))
      : patch.muutos;

    return patchOrHotfix.reduce((prevData, singlePatch, singlePatchIndex) => {
      try {
        return apply_patch(prevData, [singlePatch]);
      } catch (e) {
        console.log(
          `Versionumero ${patch.versionumero} aiheuttaa virheen: ${e instanceof Error ? e.message : String(e)}\n`,
        );
        console.log(`muutos[${singlePatchIndex}]:`, singlePatch);
        writeFileSync("dump.json", JSON.stringify(prevData, null, 2));
        writeFileSync(hotfix, JSON.stringify(patch.muutos, null, 2));
        process.exit(1);
      }
    }, data);
  }, {});

  console.log("Historia on kunnossa.");
};

run().catch(console.error);
