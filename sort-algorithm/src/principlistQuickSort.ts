import type { SortAlgorithm } from "./types"

/**
 * Array.concatを関数型っぽい形にするための糖衣
 */
const concat = <T>(
  arr: ReadonlyArray<T>,
  ...others: ReadonlyArray<T | ReadonlyArray<T>>
) => arr.concat(...others)

/**
 * { }とreturn文を無くしたい原理主義者のコード
 * 文が無い
 *  = 式の組み合わせだけで表現している
 *  = 考え方を最も間違いなく表現している
 */
export const principlistQuickSort: SortAlgorithm = (arr) =>
  arr.length <= 1
    ? arr
    : concat(
        principlistQuickSort(arr.filter((num) => num < arr[0])),
        arr[0],
        principlistQuickSort(arr.slice(1).filter((num) => num >= arr[0]))
      )
