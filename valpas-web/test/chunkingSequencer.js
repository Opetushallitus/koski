const Sequencer = require("@jest/test-sequencer").default

class ChunkingSequencer extends Sequencer {
  sort(tests) {
    const chunking = (process.env.TEST_CHUNK || "").match(/(\d+)\/(\d+)/)
    const sortedTests = super.sort(tests)

    if (chunking) {
      const chunkIndex = parseInt(chunking[1], 10) - 1
      const chunkCount = parseInt(chunking[2], 10)
      const testChunk = sortedTests.filter(
        (_test, index) => index % chunkCount === chunkIndex
      )

      console.log(
        `Running test chunk ${chunking[0]} (${testChunk.length} of ${sortedTests.length} tests):`
      )
      testChunk.forEach((test) => console.log(test.path))

      return process.env.LIST_ONLY ? [] : testChunk
    } else {
      return sortedTests
    }
  }
}

module.exports = ChunkingSequencer
