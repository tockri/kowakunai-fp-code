import $ from "jquery"
import { pipe } from "tools"

export interface State {
  value: string
  isValid: boolean
  errorMessage: string
}

/**
 * DOMからStateを読み取る
 */
const getState = (ipt: JQuery): State => {
  return {
    value: ipt.val() as string,
    isValid: true,
    errorMessage: ""
  }
}

/**
 * StateをDOMに書き戻す
 */
const setState = (ipt: JQuery, helper: JQuery, state: State) => {
  ipt.val(state.value)
  if (state.isValid) {
    ipt.addClass("valid")
    ipt.removeClass("invalid")
  } else {
    ipt.addClass("invalid")
    ipt.removeClass("valid")
    helper.attr("data-error", state.errorMessage)
  }
}

type CheckFunction = (state: State) => State

/**
 * メインロジックをラップする。
 * I/Oはここに局所化
 */
const validate = (inputSel: string, helperSel: string, func: CheckFunction) => {
  const ipt = $(inputSel)
  const helper = $(helperSel)
  const state = getState(ipt)
  const checked = func(state)
  setState(ipt, helper, checked)
  checkSubmitable()
}

/**
 * 必須チェック
 */
const checkNonEmpty =
  (errorMessage: string): CheckFunction =>
  (state) => {
    if (state.value) {
      return state
    } else {
      return {
        ...state,
        isValid: false,
        errorMessage
      }
    }
  }

/**
 * 名前の検証ロジック
 */
const nameLogic = checkNonEmpty("氏名を入力してください")

/**
 * 名前のバリデーション
 */
function validateName() {
  validate("#name", "#name-helper", nameLogic)
}

/**
 * 全角を半角に変換する
 */
const replaceZenkakuToHankaku: CheckFunction = (state) => ({
  ...state,
  value: state.value
    .replace(/[Ａ-Ｚａ-ｚ０-９＠．]/g, (s) =>
      String.fromCharCode(s.charCodeAt(0) - 65248)
    )
    .replace(/[ー−―‐]/g, "-")
})

/**
 * 郵便番号にハイフンが含まれてなければ入れる
 */
const normalizeZipCode: CheckFunction = (state) => {
  const m = state.value.match(/^(\d{3})-?(\d{4})$/)
  const value = m ? `${m[1]}-${m[2]}` : state.value
  return {
    ...state,
    value
  }
}

/**
 * 正規表現に一致していなければエラーにするCheckFunctionを返す
 */
const checkPattern =
  (pattern: RegExp, errorMessage: string): CheckFunction =>
  (state) => {
    if (!state.isValid) {
      // すでにValidでなくなっている場合は素通し
      return state
    } else {
      if (state.value.match(pattern)) {
        return state
      } else {
        return {
          ...state,
          isValid: false,
          errorMessage
        }
      }
    }
  }

/**
 * 郵便番号が正しいかチェックする
 */
const checkValidZipCode: CheckFunction = checkPattern(
  /^(\d{3})-(\d{4})$/,
  "000-0000の形式で入力してください"
)

/**
 * 郵便番号の検証ロジック
 */
const zipLogic: CheckFunction = pipe(
  checkNonEmpty("郵便番号を入力してください"),
  replaceZenkakuToHankaku,
  normalizeZipCode,
  checkValidZipCode
)

/**
 * 郵便番号のバリデーション
 */
function validateZip() {
  validate("#zip", "#zip-helper", zipLogic)
}

/**
 * 住所の検証ロジック
 */
const addressLogic = checkNonEmpty("住所を入力してください")

/**
 * 住所のバリデーション
 */
function validateAddress() {
  validate("#address", "#address-helper", addressLogic)
}

/**
 * 大文字→小文字への変換
 */
const convertToLowercase: CheckFunction = (state) => ({
  ...state,
  value: state.value.toLowerCase()
})

/**
 * メールアドレスの形式チェック
 */
const checkValidMail: CheckFunction = checkPattern(
  /^[\w.]+@[\w.]+[^.]$/,
  "メールアドレスの形式が正しくありません"
)

/**
 * メールアドレスの検証ロジック
 */
const mailLogic = pipe(
  checkNonEmpty("メールアドレスを入力してください"),
  replaceZenkakuToHankaku,
  convertToLowercase,
  checkValidMail
)

/**
 * メールアドレスのバリデーション
 */
function validateMail() {
  validate("#mail", "#mail-helper", mailLogic)
}

/**
 * 送信していいかチェックしつつ、submit-buttonのdisabledをきりかえる
 * @return よければtrue
 */
function checkSubmitable() {
  if ($(".validate").filter(".valid").length === $(".validate").length) {
    $("#submit-button").prop("disabled", false)
    return true
  } else {
    $("#submit-button").prop("disabled", true)
    return false
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const M: any
// eslint-disable-next-line @typescript-eslint/no-explicit-any
declare const window: any

// 初期化：テスト時に実行されない工夫
if (typeof window === "object") {
  $(function () {
    // Materializeのデフォルト挙動をOFFするおまじない
    M.validate_field = window.validate_field = function () {
      return
    }
    // changeイベントを登録
    $("#name").on("change", validateName)
    $("#address").on("change", validateAddress)
    $("#zip").on("change", validateZip)
    $("#mail").on("change", validateMail)
    $("#submit-button").on("click", () => {
      if (checkSubmitable()) {
        alert("フォーム送信！")
      }
    })
  })
}

export const Form_forTestOnly = {
  replaceZenkakuToHankaku,
  normalizeZipCode,
  nameLogic,
  checkValidZipCode,
  zipLogic,
  addressLogic,
  convertToLowercase,
  checkValidMail,
  mailLogic
}
