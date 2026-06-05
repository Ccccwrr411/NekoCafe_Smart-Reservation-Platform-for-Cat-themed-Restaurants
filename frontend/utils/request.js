// utils/request.js
// ====================================================================
// 统一请求封装：开发阶段用 mock，后端好了改 USE_MOCK = false 即可
// ====================================================================

const app = getApp()

// ⚙️ 开关：true = 用假数据；false = 调真实后端
const USE_MOCK = true

/**
 * 发起请求（统一入口）
 * @param {string} url     接口路径，如 '/api/stores'
 * @param {string} method  HTTP 方法，默认 GET
 * @param {object} data    请求体 / 查询参数
 * @returns {Promise}      resolve(responseData)
 */
function request(url, method = 'GET', data = {}) {
  if (USE_MOCK) {
    return mockRequest(url, data)
  } else {
    return realRequest(url, method, data)
  }
}

// ─── Mock 模式 ──────────────────────────────────────────────────────
function mockRequest(url, data) {
  return new Promise((resolve, reject) => {
    // 模拟 300ms 网络延迟，更贴近真实体验
    setTimeout(() => {
      try {
        const mockData = require('./mock.js')
        // 解析 URL：分离路径和查询参数
        const [cleanUrl, queryString] = url.split('?')
        // 将查询参数解析为对象
        const queryParams = {}
        if (queryString) {
          queryString.split('&').forEach(pair => {
            const [k, v] = pair.split('=')
            queryParams[decodeURIComponent(k)] = decodeURIComponent(v || '')
          })
        }
        const handler = mockData[cleanUrl]
        if (handler !== undefined) {
          // 支持动态 mock：如果 handler 是函数，传入 queryParams + request data
          if (typeof handler === 'function') {
            resolve(handler(queryParams, data))
          } else {
            resolve(handler)
          }
        } else {
          console.warn('[Mock] 未找到接口数据：', cleanUrl)
          resolve({ code: 404, message: '接口未定义（mock）', data: null })
        }
      } catch (e) {
        reject(e)
      }
    }, 300)
  })
}

// ─── 真实请求模式 ────────────────────────────────────────────────────
function realRequest(url, method, data) {
  const baseUrl = app.globalData.baseUrl || 'http://172.20.10.3:8081'
  const token = wx.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // token 过期，跳转登录
          wx.navigateTo({ url: '/pages/login/login' })
          reject(new Error('未授权，请重新登录'))
        } else {
          reject(new Error(`请求失败：${res.statusCode}`))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

// ─── 语法糖 ──────────────────────────────────────────────────────────
const get  = (url, data) => request(url, 'GET', data)
const post = (url, data) => request(url, 'POST', data)
const put  = (url, data) => request(url, 'PUT', data)
const del  = (url, data) => request(url, 'DELETE', data)

module.exports = { request, get, post, put, del }
