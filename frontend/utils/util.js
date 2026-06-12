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
  if (km == null || isNaN(km)) return '未知距离'
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
  booked:                  { text: '已预约', color: '#4CAF50', bg: '#E8F5E9' },
  pending:                 { text: '待确认', color: '#FF9800', bg: '#FFF3E0' },
  confirmed:               { text: '用餐中', color: '#4CAF50', bg: '#E8F5E9' },
  occupied:                { text: '进行中', color: '#E74C3C', bg: '#FFEBEE' },
  completed:               { text: '已完成', color: '#888888', bg: '#F5F5F5' },
  cancel_booking:          { text: '取消预约', color: '#F44336', bg: '#FFEBEE' },
  cancel_order:            { text: '取消订单', color: '#F44336', bg: '#FFEBEE' },
  after_sales_pending:     { text: '售后中', color: '#FF9800', bg: '#FFF3E0' },
  after_sales_rejected:    { text: '拒绝售后', color: '#F44336', bg: '#FFEBEE' },
  after_sales_completed:   { text: '售后完成', color: '#888888', bg: '#F5F5F5' }
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
