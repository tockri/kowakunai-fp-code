import { atom } from "jotai"
import { pipe } from "tools"

/**
 * 各プロパティの状態
 */
export type State = Readonly<{
  value: string
  isValid: boolean
  errorMessage: string
}>

/**
 * プロパティに発生するアクション
 */
export type Action = Readonly<{
  type: "change" | "blur"
  value: string
}>

/**
 * Stateを更新するReducer
 */
type CheckReducer = (state: State, action: Action) => State

/**
 * Atomインスタンスを生成する
 */
const makeAtom = (reducer: CheckReducer) => {
  // 値を保持するための隠しAtom
  const store = atom<State>({
    value: "",
    isValid: true,
    errorMessage: "",
  })
  // Reducerを扱うインターフェイスとなるAtom
  return atom<State, [Action], void>(
    (get) => get(store),
    (get, set, action) => {
      const curr = get(store)
      const nextValue = reducer(curr, action)
      set(store, nextValue)
    },
  )
}

/**
 * Stateを検証する関数
 */
type CheckFunction = (state: State) => State

/**
 * CheckFunctionを組み合わせてCheckReducerを返す
 * @param options onChange: changeアクションで実行する関数, onBlur: blurアクションで実行する関数
 */
const makeReducer =
  (options: {
    onChange?: CheckFunction
    onBlur?: CheckFunction
  }): CheckReducer =>
  (state, action) => {
    const nextState: State = {
      ...state,
      value: action.value,
    }
    if (action.type === "change" && options.onChange) {
      return options.onChange(nextState)
    } else if (action.type === "blur" && options.onBlur) {
      return options.onBlur(nextState)
    } else {
      return state
    }
  }

/**
 * 必須チェック
 */
const checkNonEmpty =
  (errorMessage: string): CheckFunction =>
  (state) => {
    if (state.value) {
      return {
        value: state.value,
        isValid: true,
        errorMessage: "",
      }
    } else {
      return {
        ...state,
        isValid: false,
        errorMessage,
      }
    }
  }

/**
 * 名前のReducer
 */
const nameReducer = makeReducer({
  onChange: checkNonEmpty("氏名を入力してください"),
})

/**
 * 名前の状態Atom
 */
const nameAtom = makeAtom(nameReducer)

/**
 * 全角を半角に変換する
 */
const replaceZenkakuToHankaku: CheckFunction = (state) => ({
  ...state,
  value: state.value
    .replace(/[Ａ-Ｚａ-ｚ０-９＠．]/g, (s) =>
      String.fromCharCode(s.charCodeAt(0) - 65248),
    )
    .replace(/[ー−―‐]/g, "-"),
})

/**
 * 郵便番号にハイフンが含まれてなければ入れる
 */
const normalizeZipCode: CheckFunction = (state) => {
  const m = state.value.match(/^(\d{3})-?(\d{4})$/)
  const value = m ? `${m[1]}-${m[2]}` : state.value
  return {
    ...state,
    value,
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
          errorMessage,
        }
      }
    }
  }

/**
 * 郵便番号が正しいかチェックする
 */
const checkValidZipCode: CheckFunction = checkPattern(
  /^(\d{3})-(\d{4})$/,
  "000-0000の形式で入力してください",
)

/**
 * 郵便番号の検証ロジック
 */
const zipReducer: CheckReducer = makeReducer({
  onBlur: pipe(replaceZenkakuToHankaku, normalizeZipCode, checkValidZipCode),
  onChange: checkNonEmpty("郵便番号を入力してください"),
})

/**
 * 郵便番号のAtom
 */
const zipAtom = makeAtom(zipReducer)

/**
 * 住所の検証ロジック
 */
const addressReducer = makeReducer({
  onChange: checkNonEmpty("住所を入力してください"),
})

/**
 * 住所のAtom
 */
const addressAtom = makeAtom(addressReducer)

/**
 * 大文字→小文字への変換
 */
const convertToLowercase: CheckFunction = (state) => ({
  ...state,
  value: state.value.toLowerCase(),
})

/**
 * メールアドレスの形式チェック
 */
const checkValidMail: CheckFunction = checkPattern(
  /^[\w.]+@[\w.]+\.\w+$/,
  "メールアドレスの形式が正しくありません",
)

/**
 * メールアドレスの検証ロジック
 */
const mailReducer = makeReducer({
  onChange: checkNonEmpty("メールアドレスを入力してください"),
  onBlur: pipe(replaceZenkakuToHankaku, convertToLowercase, checkValidMail),
})

/**
 * メールアドレスのAtom
 */
const mailAtom = makeAtom(mailReducer)

/**
 * 渡されたStateがすべてisValue===trueならtrueを返す
 */
const allValid = (...states: ReadonlyArray<State>): boolean =>
  states.every((s) => s.value && s.isValid)

/**
 * 送信ボタンのAtom
 */
const submitButtonAtom = atom((get) => {
  const name = get(nameAtom)
  const zip = get(zipAtom)
  const address = get(addressAtom)
  const mail = get(mailAtom)
  return allValid(name, zip, address, mail)
})

export const FormState = {
  nameAtom,
  zipAtom,
  addressAtom,
  mailAtom,
  submitButtonAtom,
}

export const FormState_forTestOnly = {
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
}
