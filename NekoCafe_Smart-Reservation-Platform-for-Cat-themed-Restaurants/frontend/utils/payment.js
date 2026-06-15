// utils/payment.js
// 微信支付参数归一化 + 调起支付

/**
 * 判断是否为 Mock/无效的支付参数（不能调起真实微信支付）
 */
function isMockPayInfo(payInfo) {
  if (!payInfo) return true
  const pkg = payInfo.package || payInfo.prepay_id || ''
  const sign = payInfo.paySign || payInfo.pay_sign || ''
  return (
    String(pkg).includes('mock_prepay_id') ||
    String(sign).startsWith('mock_') ||
    String(sign) === 'mock_pay_sign_20260603'
  )
}

/**
 * 将后端返回的 payInfo 归一化为 wx.requestPayment 所需字段
 * 兼容 snake_case / camelCase 差异
 */
function normalizePayInfo(raw) {
  if (!raw) return null
  const pkg = raw.package || raw.prepay_id || raw.prepayId || ''
  const normalizedPackage = String(pkg).startsWith('prepay_id=')
    ? String(pkg)
    : (pkg ? `prepay_id=${pkg}` : '')

  return {
    timeStamp: String(raw.timeStamp || raw.timestamp || raw.time_stamp || ''),
    nonceStr: String(raw.nonceStr || raw.nonce_str || ''),
    package: normalizedPackage,
    signType: raw.signType || raw.sign_type || 'RSA',
    paySign: String(raw.paySign || raw.pay_sign || '')
  }
}

/**
 * 调起微信支付；Mock 参数时走模拟支付流程
 */
function requestWxPayment(payInfo, options = {}) {
  const normalized = normalizePayInfo(payInfo)
  const onSuccess = options.onSuccess || (() => {})
  const onFail = options.onFail || (() => {})
  const onCancel = options.onCancel || (() => {})

  if (!normalized || !normalized.package || !normalized.paySign) {
    onFail({ errMsg: 'payInfo 参数不完整' })
    return
  }

  if (isMockPayInfo(normalized)) {
    wx.showModal({
      title: '模拟支付',
      content: `当前为开发/mock 模式，无法调起真实微信支付。\n\n是否模拟支付成功？`,
      confirmText: '模拟成功',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) onSuccess()
        else onCancel()
      }
    })
    return
  }

  wx.requestPayment({
    ...normalized,
    success: onSuccess,
    fail: (err) => {
      if (err.errMsg && err.errMsg.includes('cancel')) onCancel(err)
      else onFail(err)
    }
  })
}

/**
 * 获取 wx.login code（对接真实后端下单/支付时需要）
 */
function getWxLoginCode() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) resolve(res.code)
        else reject(new Error('wx.login 未返回 code'))
      },
      fail: reject
    })
  })
}

module.exports = {
  isMockPayInfo,
  normalizePayInfo,
  requestWxPayment,
  getWxLoginCode
}
