// utils/request.js
// ====================================================================
// 统一请求封装：开发阶段用 mock，后端好了改 USE_MOCK = false 即可
// ====================================================================

/**
 * 是否使用 mock 数据（优先读 app.globalData.useMock，默认 false 对接沙箱后端）
 */
function isUseMock() {
  try {
    const app = getApp()
    return app.globalData.useMock === true
  } catch (e) {
    return false
  }
}

/**
 * 发起请求（统一入口）
 * @param {string} url     接口路径，如 '/api/stores'
 * @param {string} method  HTTP 方法，默认 GET
 * @param {object} data    请求体 / 查询参数
 * @returns {Promise}      resolve(responseData)
 */
function request(url, method = 'GET', data = {}) {
  if (isUseMock()) {
    return mockRequest(url, data)
  }
  return realRequest(url, method, data)
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
  const app = getApp()
  const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
  const isLoginRequest = (url === '/api/auth/login')
  const token = wx.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    const header = {
      'Content-Type': 'application/json'
    }

    // 非登录接口：必须有 token 才放行（先登录，再请求数据）
    if (!isLoginRequest) {
      if (!token) {
        wx.reLaunch({ url: '/pages/login/login' })
        // 用 resolve 返回安全响应（而非 reject），避免未捕获异常导致页面白屏
        // reLaunch 会异步执行跳转，页面在此期间收到 { code: -1 } 可安全渲染
        resolve({ code: -1, message: '未登录', data: null })
        return
      }
      header['Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: baseUrl + url,
      method,
      data,
      header,
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // token 过期，清除本地登录态并跳转登录
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.removeStorageSync('userRole')
          if (app.globalData) {
            app.globalData.userInfo = null
            app.globalData.userRole = null
          }
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期，请重新登录'))
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

module.exports = { request, get, post, put, del, isUseMock }
