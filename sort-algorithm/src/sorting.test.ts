import { describe, test, expect } from "bun:test"
import { bubbleSort } from "./bubbleSort"
import type { SortAlgorithm } from "./types"
import { quickSort } from "./quickSort"
import { millionaireQuickSort } from "./millionaireQuickSort"
import { billionaireQuickSort } from "./billionaireQuickSort"
import { principlistQuickSort } from "./principlistQuickSort"

const testAlgorighm = (algorithm: SortAlgorithm) => {
  const pairs = [
    { init: [], result: [] },
    { init: [1], result: [1] },
    { init: [9, 1, 2, 10, 8, 3], result: [1, 2, 3, 8, 9, 10] },
    { init: [2, 1, 1, 1], result: [1, 1, 1, 2] },
    {
      init: [10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2],
      result: [-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
    }
  ]
  pairs.forEach((pair) => {
    expect(algorithm(pair.init)).toStrictEqual(pair.result)
  })
}

describe("each algorighm", () => {
  test("bubbleSort", () => {
    testAlgorighm(bubbleSort)
  })
  test("quickSort", () => {
    testAlgorighm(quickSort)
  })
  test("millionaireQuickSort", () => {
    testAlgorighm(millionaireQuickSort)
  })
  test("billionaireQuickSort", () => {
    testAlgorighm(billionaireQuickSort)
  })
  test("principlistQuickSort", () => {
    testAlgorighm(principlistQuickSort)
  })
})
