import { describe, test, expect } from "bun:test"
import { Form_forTestOnly } from "./form"
const { replaceZenkakuToHankaku, normalizeZipCode, nameLogic } =
  Form_forTestOnly

describe("replaceZenkakuToHankaku", () => {
  test("全角", () => {
    expect(replaceZenkakuToHankaku("０１２ー３４５６")).toBe("012-3456")
    expect(replaceZenkakuToHankaku("ー−―‐")).toBe("----")
    expect(replaceZenkakuToHankaku("ＢＡＬ")).toBe("BAL")
  })
})

describe("normalizeZipCode", () => {
  test("ハイフンなし", () => {
    expect(normalizeZipCode("1234567")).toBe("123-4567")
    expect(normalizeZipCode("123-4567")).toBe("123-4567")
    expect(normalizeZipCode("12345678")).toBe("12345678")
  })
})

describe("nameLogic", () => {
  test("空", () => {
    expect(
      nameLogic({
        value: ""
      })
    ).toStrictEqual({
      value: "",
      isValid: false,
      errorMessage: "氏名を入力してください"
    })
  })
})
