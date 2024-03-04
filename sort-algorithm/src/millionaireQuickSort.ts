import type { SortAlgorithm } from "./types"

/*
 * クイックソートの考え方をわかりやすく表現したコード
 * メモリ使用量を犠牲にして書きやすさをとった
 */
export const millionaireQuickSort: SortAlgorithm = (arr) => {
  if (arr.length <= 1) {
    return arr
  }
  const smaller: Array<number> = []
  const larger: Array<number> = []
  const pivot = arr[0]
  for (let i = 1; i < arr.length; i++) {
    const num = arr[i]
    if (num < pivot) {
      smaller.push(num)
    } else {
      larger.push(num)
    }
  }
  const sortedSmaller = millionaireQuickSort(smaller)
  const sortedLarger = millionaireQuickSort(larger)
  return sortedSmaller.concat(pivot, sortedLarger)
}
