import { describe, test, expect } from "bun:test"
import { Form_forTestOnly, State } from "./form"
const {
  replaceZenkakuToHankaku,
  normalizeZipCode,
  nameLogic,
  checkValidZipCode,
  zipLogic,
  addressLogic,
  convertToLowercase,
  checkValidMail,
  mailLogic
} = Form_forTestOnly

const state = (value: string, isValid = true, errorMessage = ""): State => ({
  value,
  isValid,
  errorMessage
})

describe("replaceZenkakuToHankaku", () => {
  test("全角", () => {
    expect(replaceZenkakuToHankaku(state("０１２ー３４５６"))).toStrictEqual(
      state("012-3456")
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
      state("123-4567")
    )
  })

  test("正しくない郵便番号", () => {
    expect(checkValidZipCode(state("123-45678"))).toStrictEqual(
      state("123-45678", false, "000-0000の形式で入力してください")
    )
    expect(checkValidZipCode(state("a"))).toStrictEqual(
      state("a", false, "000-0000の形式で入力してください")
    )
    expect(checkValidZipCode(state("1234567"))).toStrictEqual(
      state("1234567", false, "000-0000の形式で入力してください")
    )
  })
})

describe("zipLogic", () => {
  test("正しい郵便番号", () => {
    expect(zipLogic(state("123-4567"))).toStrictEqual(state("123-4567"))
    expect(zipLogic(state("1234567"))).toStrictEqual(state("123-4567"))
  })

  test("空", () => {
    expect(zipLogic(state(""))).toStrictEqual(
      state("", false, "郵便番号を入力してください")
    )
  })

  test("正しくない郵便番号", () => {
    expect(zipLogic(state("123-45678"))).toStrictEqual(
      state("123-45678", false, "000-0000の形式で入力してください")
    )
    expect(zipLogic(state("a"))).toStrictEqual(
      state("a", false, "000-0000の形式で入力してください")
    )
  })
})

describe("nameLogic", () => {
  test("空", () => {
    expect(nameLogic(state(""))).toStrictEqual(
      state("", false, "氏名を入力してください")
    )
  })

  test("空じゃない", () => {
    expect(nameLogic(state("何か書いた"))).toStrictEqual(state("何か書いた"))
  })
})

describe("addressLogic", () => {
  test("空", () => {
    expect(addressLogic(state(""))).toStrictEqual(
      state("", false, "住所を入力してください")
    )
  })

  test("空じゃない", () => {
    expect(addressLogic(state("何か書いた"))).toStrictEqual(state("何か書いた"))
  })
})

describe("convertToLowercase", () => {
  test("大文字", () => {
    expect(convertToLowercase(state("AbcDあいうえお"))).toStrictEqual(
      state("abcdあいうえお")
    )
  })
})

describe("checkValidMail", () => {
  test("正しいメールアドレス形式", () => {
    expect(checkValidMail(state("em@c.com"))).toStrictEqual(state("em@c.com"))
  })
  test("正しくない", () => {
    expect(checkValidMail(state("em@com."))).toStrictEqual(
      state("em@com.", false, "メールアドレスの形式が正しくありません")
    )
    expect(checkValidMail(state("emあいうえお@com"))).toStrictEqual(
      state("emあいうえお@com", false, "メールアドレスの形式が正しくありません")
    )
    expect(checkValidMail(state("com"))).toStrictEqual(
      state("com", false, "メールアドレスの形式が正しくありません")
    )
  })
})

describe("mailLogic", () => {
  test("正しいメールアドレス形式", () => {
    expect(mailLogic(state("em@c.com"))).toStrictEqual(state("em@c.com"))
    expect(mailLogic(state("EM@c.com"))).toStrictEqual(state("em@c.com"))
    expect(mailLogic(state("ＡＢ@c.com"))).toStrictEqual(state("ab@c.com"))
  })
  test("空", () => {
    expect(mailLogic(state(""))).toStrictEqual(
      state("", false, "メールアドレスを入力してください")
    )
  })
  test("正しくない", () => {
    expect(mailLogic(state("em@com."))).toStrictEqual(
      state("em@com.", false, "メールアドレスの形式が正しくありません")
    )
    expect(mailLogic(state("ＡＢあいうえお@com"))).toStrictEqual(
      state("abあいうえお@com", false, "メールアドレスの形式が正しくありません")
    )
    expect(mailLogic(state("COM"))).toStrictEqual(
      state("com", false, "メールアドレスの形式が正しくありません")
    )
  })
})
