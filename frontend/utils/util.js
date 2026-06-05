// utils/util.js
// 通用工具函数

/**
 * 格式化时间戳为 YYYY-MM-DD HH:mm
 */
const formatTime = date => {
  const d = typeof date === 'string' ? new Date(date) : date
  const year   = d.getFullYear()
  const month  = String(d.getMonth() + 1).padStart(2, '0')
  const day    = String(d.getDate()).padStart(2, '0')
  const hour   = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/**
 * 格式化价格：38 → ¥38.00
 */
const formatPrice = (price) => `¥${Number(price).toFixed(2)}`

/**
 * 格式化距离：1.2 → 1.2km；0.8 → 800m
 */
const formatDistance = (km) => {
  if (km < 1) return `${Math.round(km * 1000)}m`
  return `${km.toFixed(1)}km`
}

/**
 * Haversine 公式计算两点间距离（km）
 * @param {number} lat1
 * @param {number} lng1
 * @param {number} lat2
 * @param {number} lng2
 * @returns {number} 距离（km）
 */
const calcDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

/**
 * 订单状态映射
 */
const ORDER_STATUS_MAP = {
  pending:    { text: '待确认', color: '#FF9800' },
  confirmed:  { text: '已确认', color: '#4CAF50' },
  occupied:   { text: '进行中', color: '#E74C3C' },
  completed:  { text: '已完成', color: '#888888' },
  cancelled:  { text: '已取消', color: '#F44336' }
}

/**
 * 桌位状态映射
 */
const TABLE_STATUS_MAP = {
  available:   { text: '可用', color: '#4CAF50', bg: '#E8F5E9' },
  booked:      { text: '已约', color: '#F44336', bg: '#FFEBEE' },
  maintenance: { text: '维护', color: '#888888', bg: '#F5F5F5' }
}

/**
 * 计算购物车总价
 */
const calcCartTotal = (items) =>
  items.reduce((sum, item) => sum + item.price * item.qty, 0)

/**
 * 生成预约时间段（10:00 ~ 21:00，每30分钟）
 */
const genTimeSlots = () => {
  const slots = []
  for (let h = 10; h <= 21; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
    if (h < 21) slots.push(`${String(h).padStart(2, '0')}:30`)
  }
  return slots
}

module.exports = {
  formatTime,
  formatPrice,
  formatDistance,
  calcDistance,
  ORDER_STATUS_MAP,
  TABLE_STATUS_MAP,
  calcCartTotal,
  genTimeSlots
}
