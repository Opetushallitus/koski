const sourceDir = "src"
const snapshotDir = "test/snapshots"

module.exports = {
  /** @type {(testPath: string, snapshotExtension: string) => string} */
  resolveSnapshotPath: (testPath, snapshotExtension) =>
    testPath
      .replace(/\.test\.([tj]sx?)/, `.test.$1${snapshotExtension}`)
      .replace(sourceDir, snapshotDir),
  /** @type {(snapshotFilePath: string, snapshotExtension: string) => string} */
  resolveTestPath: (snapshotFilePath, snapshotExtension) =>
    snapshotFilePath
      .replace(snapshotExtension, "")
      .replace(snapshotDir, sourceDir),
  testPathForConsistencyCheck: "src/components/basrco/Some.test.tsx",
}
