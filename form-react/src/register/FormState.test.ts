import { describe, test, expect } from "bun:test"
import { Action, FormState_forTestOnly, State } from "./FormState"
const {
  replaceZenkakuToHankaku,
  normalizeZipCode,
  nameReducer,
  checkValidZipCode,
  zipReducer,
  addressReducer,
  convertToLowercase,
  checkValidMail,
  mailReducer,
  allValid,
} = FormState_forTestOnly

const state = (value: string, isValid = true, errorMessage = ""): State => ({
  value,
  isValid,
  errorMessage,
})

const change = (value: string): Action => ({
  type: "change",
  value,
})
const blur = (value: string): Action => ({
  type: "blur",
  value,
})

describe("replaceZenkakuToHankaku", () => {
  test("全角", () => {
    expect(replaceZenkakuToHankaku(state("０１２ー３４５６"))).toStrictEqual(
      state("012-3456"),
    )
    expect(replaceZenkakuToHankaku(state("ー−―‐"))).toStrictEqual(state("----"))
    expect(replaceZenkakuToHankaku(state("ＢＡＬ"))).toStrictEqual(state("BAL"))
  })
})

describe("normalizeZipCode", () => {
  test("ハイフンなし", () => {
    expect(normalizeZipCode(state("1234567"))).toStrictEqual(state("123-4567"))
    expect(normalizeZipCode(state("123-4567"))).toStrictEqual(state("123-4567"))
    expect(normalizeZipCode(state("12345678"))).toStrictEqual(state("12345678"))
  })
})

describe("checkValidZipCode", () => {
  test("正しい郵便番号", () => {
    expect(checkValidZipCode(state("123-4567"))).toStrictEqual(
      state("123-4567"),
    )
  })

  test("正しくない郵便番号", () => {
    expect(checkValidZipCode(state("123-45678"))).toStrictEqual(
      state("123-45678", false, "000-0000の形式で入力してください"),
    )
    expect(checkValidZipCode(state("a"))).toStrictEqual(
      state("a", false, "000-0000の形式で入力してください"),
    )
    expect(checkValidZipCode(state("1234567"))).toStrictEqual(
      state("1234567", false, "000-0000の形式で入力してください"),
    )
  })
})

describe("zipReducer", () => {
  test("正しい郵便番号", () => {
    expect(zipReducer(state("123-456"), change("123-4567"))).toStrictEqual(
      state("123-4567"),
    )
    expect(zipReducer(state("1234567"), blur("1234567"))).toStrictEqual(
      state("123-4567"),
    )
  })

  test("空", () => {
    expect(zipReducer(state(""), change(""))).toStrictEqual(
      state("", false, "郵便番号を入力してください"),
    )
  })

  test("正しくない郵便番号", () => {
    expect(zipReducer(state("123-4567"), blur("123-45678"))).toStrictEqual(
      state("123-45678", false, "000-0000の形式で入力してください"),
    )
    expect(zipReducer(state("a"), blur("a"))).toStrictEqual(
      state("a", false, "000-0000の形式で入力してください"),
    )
  })
})

describe("nameReducer", () => {
  test("空", () => {
    expect(nameReducer(state(""), change(""))).toStrictEqual(
      state("", false, "氏名を入力してください"),
    )
  })

  test("空じゃない", () => {
    expect(nameReducer(state("書いた"), change("何か書いた"))).toStrictEqual(
      state("何か書いた"),
    )
  })
})

describe("addressReducer", () => {
  test("空", () => {
    expect(addressReducer(state(""), change(""))).toStrictEqual(
      state("", false, "住所を入力してください"),
    )
  })

  test("空じゃない", () => {
    expect(
      addressReducer(state("何か書いた"), change("何か書いた")),
    ).toStrictEqual(state("何か書いた"))
  })
})

describe("convertToLowercase", () => {
  test("大文字", () => {
    expect(convertToLowercase(state("AbcDあいうえお"))).toStrictEqual(
      state("abcdあいうえお"),
    )
  })
})

describe("checkValidMail", () => {
  test("正しいメールアドレス形式", () => {
    expect(checkValidMail(state("em@c.com"))).toStrictEqual(state("em@c.com"))
  })
  test("正しくない", () => {
    expect(checkValidMail(state("em@com."))).toStrictEqual(
      state("em@com.", false, "メールアドレスの形式が正しくありません"),
    )
    expect(checkValidMail(state("emあいうえお@com"))).toStrictEqual(
      state(
        "emあいうえお@com",
        false,
        "メールアドレスの形式が正しくありません",
      ),
    )
    expect(checkValidMail(state("com"))).toStrictEqual(
      state("com", false, "メールアドレスの形式が正しくありません"),
    )
  })
})

describe("mailReducer", () => {
  test("正しいメールアドレス形式", () => {
    expect(mailReducer(state(""), blur("em@c.com"))).toStrictEqual(
      state("em@c.com"),
    )
    expect(mailReducer(state(""), blur("EM@c.com"))).toStrictEqual(
      state("em@c.com"),
    )
    expect(mailReducer(state(""), blur("ＡＢ@c.com"))).toStrictEqual(
      state("ab@c.com"),
    )
  })
  test("空", () => {
    expect(mailReducer(state(""), change(""))).toStrictEqual(
      state("", false, "メールアドレスを入力してください"),
    )
  })
  test("正しくない", () => {
    expect(mailReducer(state(""), blur("em@com."))).toStrictEqual(
      state("em@com.", false, "メールアドレスの形式が正しくありません"),
    )
    expect(mailReducer(state(""), blur("ＡＢあいうえお@com"))).toStrictEqual(
      state(
        "abあいうえお@com",
        false,
        "メールアドレスの形式が正しくありません",
      ),
    )
    expect(mailReducer(state(""), blur("COM"))).toStrictEqual(
      state("com", false, "メールアドレスの形式が正しくありません"),
    )
  })
})

describe("allValid", () => {
  test("true", () => {
    expect(allValid(state("a"), state("b"), state("c"))).toBe(true)
  })
  test("false", () => {
    expect(allValid(state(""), state("b"), state("c"))).toBe(false)
    expect(allValid(state("a"), state("b", false), state("c"))).toBe(false)
  })
})
