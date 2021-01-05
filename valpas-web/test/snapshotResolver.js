const sourceDir = "src"
const snapshotDir = "test/snapshots"

module.exports = {
  resolveSnapshotPath: (testPath, snapshotExtension) =>
    testPath
      .replace(/\.test\.([tj]sx?)/, `.test.$1${snapshotExtension}`)
      .replace(sourceDir, snapshotDir),
  resolveTestPath: (snapshotFilePath, snapshotExtension) =>
    snapshotFilePath
      .replace(snapshotExtension, "")
      .replace(snapshotDir, sourceDir),
  testPathForConsistencyCheck: "src/components/basrco/Some.test.tsx",
}
