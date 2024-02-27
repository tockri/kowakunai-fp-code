import $ from "jquery"

/**
 * 名前のバリデーション
 */
function validateName() {
  const ipt = $("#name")
  const helper = $("#name-helper")
  if (ipt.val() !== "") {
    ipt.removeClass("invalid")
    ipt.addClass("valid")
  } else {
    ipt.removeClass("valid")
    ipt.addClass("invalid")
    helper.attr("data-error", "氏名を入力してください")
  }
  // submit-buttonのチェックもする
  checkSubmitable()
}

/**
 * 郵便番号のバリデーション
 */
function validateZip() {
  const ipt = $("#zip")
  const helper = $("#zip-helper")
  let zip = ipt.val() as string
  if (zip) {
    // 全角→半角に変換
    zip = zip
      .replace(/[Ａ-Ｚａ-ｚ０-９＠．]/g, function (s) {
        return String.fromCharCode(s.charCodeAt(0) - 65248)
      })
      .replace(/[ー−―‐]/, "-")
    const m = zip.match(/^(\d{3})-?(\d{4})$/)
    if (m) {
      zip = m[1] + "-" + m[2]
      ipt.val(zip)
      ipt.removeClass("invalid")
      ipt.addClass("valid")
    } else {
      ipt.removeClass("valid")
      ipt.addClass("invalid")
      helper.attr("data-error", "000-0000の形式で入力してください")
    }
  } else {
    ipt.removeClass("valid")
    ipt.addClass("invalid")
    helper.attr("data-error", "郵便番号を入力してください")
  }
  // submit-buttonのチェックもする
  checkSubmitable()
}
/**
 * 住所のバリデーション
 */
function validateAddress() {
  const ipt = $("#address")
  const helper = $("#address-helper")
  if (ipt.val()) {
    ipt.removeClass("invalid")
    ipt.addClass("valid")
  } else {
    ipt.removeClass("valid")
    ipt.addClass("invalid")
    helper.attr("data-error", "住所を入力してください")
  }
  // submit-buttonのチェックもする
  checkSubmitable()
}
/**
 * メールアドレスのバリデーション
 */
function validateMail() {
  const ipt = $("#mail")
  const helper = $("#mail-helper")
  let mail = ipt.val() as string
  if (mail) {
    mail = mail
      .replace(/[Ａ-Ｚａ-ｚ０-９＠．]/g, function (s) {
        return String.fromCharCode(s.charCodeAt(0) - 65248)
      })
      .replace(/[ー−―‐]/, "-")
      .toLowerCase()
    if (mail.match(/^[\w.]+@[\w.]+[^.]$/)) {
      ipt.val(mail)
      ipt.removeClass("invalid")
      ipt.addClass("valid")
    } else {
      ipt.removeClass("valid")
      ipt.addClass("invalid")
      helper.attr("data-error", "メールアドレスの形式が正しくありません。")
    }
  } else {
    ipt.removeClass("valid")
    ipt.addClass("invalid")
    helper.attr("data-error", "メールアドレスを入力してください")
  }
  // submit-buttonのチェックもする
  checkSubmitable()
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
// 初期化
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
