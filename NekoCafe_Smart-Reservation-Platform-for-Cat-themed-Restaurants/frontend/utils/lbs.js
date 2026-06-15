// utils/lbs.js
// ============================================================
// 共享 LBS 定位与距离排序模块
// 首页 / 预约页 / 点单页统一使用此模块
// ============================================================

const { calcDistance, formatDistance } = require('./util')

/**
 * Mock 模式下的模拟用户定位（北京市中心 — 天安门广场附近）
 * 避免真机 GPS 定位与北京门店坐标差异过大导致距离异常（如 1891km）
 */
const MOCK_USER_LOCATION = { lat: 39.9042, lng: 116.4074 }

/**
 * 获取用户定位（Promise 封装）
 *
 * Mock 模式：返回模拟的北京市中心定位，确保距离计算合理（几 km 级别）
 * 真实模式：调用 wx.getLocation 获取设备真实 GPS
 *
 * @returns {Promise<{lat: number, lng: number}>}
 */
function getUserLocation() {
  // Mock 模式：使用模拟北京定位
  if (isMockMode()) {
    return Promise.resolve({ ...MOCK_USER_LOCATION })
  }

  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        resolve({ lat: res.latitude, lng: res.longitude })
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 判断当前是否为 Mock 模式
 * 与 request.js 中的 isUseMock() 逻辑保持一致
 */
function isMockMode() {
  try {
    const app = getApp()
    return app.globalData.useMock === true
  } catch (e) {
    return false
  }
}

/**
 * 为门店列表计算真实距离并按「由近→远」升序排列
 *
 * @param {Array}  stores        - 门店列表（需含 lat/lng 字段）
 * @param {Object|null} userLoc  - 用户定位 { lat, lng }，为 null 表示定位失败
 * @returns {Array} 已排序的门店列表（每个元素新增 distanceText、realDistance 字段）
 *
 * 行为：
 *  - 定位成功：Haversine 计算真实千米距离 → 升序排列
 *  - 定位失败：保留原始顺序，距离统一显示「未知距离」
 *  - 个别门店缺失 lat/lng：该门店显示「未知距离」，排在列表末尾
 */
function applyDistanceAndSort(stores, userLoc) {
  if (!stores || stores.length === 0) return stores || []

  // 定位失败 / 无定位 → 保留原顺序，距离统一为「未知距离」
  if (!userLoc || userLoc.lat == null || userLoc.lng == null) {
    return stores.map(s => ({ ...s, distanceText: '未知距离', realDistance: undefined }))
  }

  // 计算真实距离
  const withDistance = stores.map(s => {
    if (s.lat != null && s.lng != null) {
      const dist = calcDistance(userLoc.lat, userLoc.lng, s.lat, s.lng)
      return { ...s, realDistance: dist, distanceText: formatDistance(dist) }
    }
    // 门店缺经纬度 → 未知距离
    return { ...s, realDistance: undefined, distanceText: '未知距离' }
  })

  // 按距离升序排列（未知距离排末尾）
  withDistance.sort((a, b) => {
    const da = a.realDistance != null ? a.realDistance : Infinity
    const db = b.realDistance != null ? b.realDistance : Infinity
    return da - db
  })

  return withDistance
}

module.exports = { getUserLocation, applyDistanceAndSort, isMockMode }
