import { describe, test, expect } from "bun:test"
import { compose, pipe, pipeAsync } from "./compose"

describe("compose", () => {
  test("combine same type funcs", () => {
    const composed = compose(
      (a: number) => a + 1,
      (a: number) => a * 2
    )
    expect(composed(1)).toBe(4)
    expect(composed(2)).toBe(6)
    expect(composed(10)).toBe(22)
  })

  test("combime different type funcs", () => {
    const composed = compose(
      (a: number) => `${a + 1}`,
      (s: string) => `s=${s}, s+1=${parseInt(s) + 1}`
    )
    expect(composed(1)).toBe("s=2, s+1=3")
    expect(composed(2)).toBe("s=3, s+1=4")
  })
})

describe("pipe", () => {
  test("combine 4 funcs", () => {
    const composed = pipe(
      (a: number) => `${a + 1}`,
      (s: string) => s + s,
      (s: string) => parseInt(s),
      (a: number) => a * 2
    )
    expect(composed(1)).toBe(44)
    expect(composed(2)).toBe(66)
  })
})

describe("pipeAsync", () => {
  test("combine sync funcs", async () => {
    const composed = pipeAsync(
      (a: number) => `${a + 1}`,
      (s: string) => s + s,
      (s: string) => parseInt(s),
      (a: number) => a * 2
    )
    expect(await composed(1)).toBe(44)
    expect(await composed(2)).toBe(66)
  })

  test("combine async funcs", async () => {
    const composed = pipeAsync(
      (a: number) => `${a + 1}`,
      (s: string) => Promise.resolve(s + s),
      (s: string) => parseInt(s),
      (a: number) =>
        new Promise((resolve) =>
          setTimeout(() => {
            resolve(a * 2)
          }, 50)
        )
    )
    expect(await composed(1)).toBe(44)
    expect(await composed(2)).toBe(66)
  })
})
