// utils/chart.js
// 微信小程序 Canvas 2D 简易图表工具
// 支持：折线图、柱状图、饼图

/**
 * 设备像素比（处理高清屏）
 */
function getPixelRatio() {
  return wx.getSystemInfoSync().pixelRatio || 2
}

/**
 * 获取 Canvas 节点和上下文
 * @param {string} selector - canvas 选择器
 * @param {object} component - 当前组件/页面实例
 * @returns {Promise<{canvas, ctx, width, height, dpr}>}
 */
function initCanvas(selector, component) {
  return new Promise(function(resolve, reject) {
    var query = component.createSelectorQuery ? component.createSelectorQuery() : wx.createSelectorQuery()
    if (component.selectOwnerComponent) {
      query = query.selectOwnerComponent ? query.selectOwnerComponent().select(selector) : query.select(selector)
    }
    query.fields({ node: true, size: true }).exec(function(res) {
      if (!res || !res[0]) {
        reject(new Error('Canvas node not found: ' + selector))
        return
      }
      var canvas = res[0].node
      var ctx = canvas.getContext('2d')
      var dpr = getPixelRatio()
      var width = res[0].width
      var height = res[0].height
      canvas.width = width * dpr
      canvas.height = height * dpr
      ctx.scale(dpr, dpr)
      resolve({ canvas: canvas, ctx: ctx, width: width, height: height, dpr: dpr })
    })
  })
}

/**
 * 计算 Y 轴刻度
 */
function calcYRange(values, tickCount) {
  tickCount = tickCount || 5
  var max = 0
  for (var i = 0; i < values.length; i++) {
    if (values[i] > max) max = values[i]
  }
  if (max === 0) max = 10
  max = Math.ceil(max * 1.15)
  var step = Math.ceil(max / tickCount)
  // 取整到合适的刻度
  if (step > 100) step = Math.ceil(step / 100) * 100
  else if (step > 10) step = Math.ceil(step / 10) * 10
  else if (step > 5) step = Math.ceil(step / 5) * 5
  else step = Math.ceil(step)
  max = step * tickCount
  var ticks = []
  for (var j = 0; j <= tickCount; j++) {
    ticks.push(j * step)
  }
  return { max: max, step: step, ticks: ticks }
}

/**
 * 绘制折线图
 * @param {CanvasRenderingContext2D} ctx
 * @param {object} options
 *   labels: string[]
 *   values: number[]
 *   width: number
 *   height: number
 *   lineColor: string (默认 '#C97E5A')
 *   fillColor: string (渐变填充色)
 *   pointColor: string
 *   labelColor: string
 *   gridColor: string
 *   padding: {top, right, bottom, left}
 */
function drawLine(ctx, options) {
  var labels = options.labels || []
  var values = options.values || []
  var w = options.width
  var h = options.height
  var pad = options.padding || { top: 20, right: 16, bottom: 40, left: 60 }
  var lineColor = options.lineColor || '#C97E5A'
  var pointColor = options.pointColor || '#C97E5A'
  var labelColor = options.labelColor || '#999'
  var gridColor = options.gridColor || '#f0e8e0'

  var plotW = w - pad.left - pad.right
  var plotH = h - pad.top - pad.bottom

  if (labels.length === 0 || values.length === 0) {
    // 画空状态
    ctx.fillStyle = '#ccc'
    ctx.font = '14px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', w / 2, h / 2)
    return
  }

  var yRange = calcYRange(values, 4)

  // 绘制网格线和 Y 轴标签
  ctx.strokeStyle = gridColor
  ctx.lineWidth = 0.5
  ctx.fillStyle = labelColor
  ctx.font = '11px sans-serif'
  ctx.textAlign = 'right'
  ctx.textBaseline = 'middle'

  for (var t = 0; t < yRange.ticks.length; t++) {
    var y = pad.top + plotH - (yRange.ticks[t] / yRange.max) * plotH
    ctx.beginPath()
    ctx.moveTo(pad.left, y)
    ctx.lineTo(w - pad.right, y)
    ctx.stroke()
    ctx.fillText(String(yRange.ticks[t]), pad.left - 8, y)
  }

  // X 轴标签
  ctx.fillStyle = labelColor
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'
  var xStep = labels.length > 1 ? plotW / (labels.length - 1) : plotW

  for (var li = 0; li < labels.length; li++) {
    var lx = pad.left + li * xStep
    ctx.fillText(labels[li], lx, h - pad.bottom + 10)
  }

  // 绘制渐变填充
  var gradient = ctx.createLinearGradient(0, pad.top, 0, pad.top + plotH)
  gradient.addColorStop(0, lineColor + '40')
  gradient.addColorStop(1, lineColor + '05')

  ctx.beginPath()
  var firstX = pad.left
  var firstY = pad.top + plotH - (values[0] / yRange.max) * plotH
  ctx.moveTo(firstX, firstY)

  for (var i = 0; i < values.length; i++) {
    var px = pad.left + i * xStep
    var py = pad.top + plotH - (values[i] / yRange.max) * plotH
    ctx.lineTo(px, py)
  }

  var lastX = pad.left + (values.length - 1) * xStep
  ctx.lineTo(lastX, pad.top + plotH)
  ctx.lineTo(firstX, pad.top + plotH)
  ctx.closePath()
  ctx.fillStyle = gradient
  ctx.fill()

  // 绘制折线
  ctx.beginPath()
  ctx.moveTo(firstX, firstY)
  for (var j = 0; j < values.length; j++) {
    var lpx = pad.left + j * xStep
    var lpy = pad.top + plotH - (values[j] / yRange.max) * plotH
    ctx.lineTo(lpx, lpy)
  }
  ctx.strokeStyle = lineColor
  ctx.lineWidth = 2.5
  ctx.lineJoin = 'round'
  ctx.stroke()

  // 绘制数据点
  for (var k = 0; k < values.length; k++) {
    var dpx = pad.left + k * xStep
    var dpy = pad.top + plotH - (values[k] / yRange.max) * plotH

    // 白色外圈
    ctx.beginPath()
    ctx.arc(dpx, dpy, 6, 0, Math.PI * 2)
    ctx.fillStyle = '#fff'
    ctx.fill()
    ctx.strokeStyle = pointColor
    ctx.lineWidth = 2.5
    ctx.stroke()

    // 实心内圈
    ctx.beginPath()
    ctx.arc(dpx, dpy, 3.5, 0, Math.PI * 2)
    ctx.fillStyle = pointColor
    ctx.fill()
  }
}

/**
 * 绘制柱状图
 * @param {CanvasRenderingContext2D} ctx
 * @param {object} options
 */
function drawBar(ctx, options) {
  var labels = options.labels || []
  var values = options.values || []
  var w = options.width
  var h = options.height
  var pad = options.padding || { top: 20, right: 16, bottom: 40, left: 60 }
  var barColor = options.barColor || '#8B5A3C'
  var labelColor = options.labelColor || '#999'
  var gridColor = options.gridColor || '#f0e8e0'

  var plotW = w - pad.left - pad.right
  var plotH = h - pad.top - pad.bottom

  if (labels.length === 0 || values.length === 0) {
    ctx.fillStyle = '#ccc'
    ctx.font = '14px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', w / 2, h / 2)
    return
  }

  var yRange = calcYRange(values, 4)

  // 网格线和 Y 轴标签
  ctx.strokeStyle = gridColor
  ctx.lineWidth = 0.5
  ctx.fillStyle = labelColor
  ctx.font = '11px sans-serif'
  ctx.textAlign = 'right'
  ctx.textBaseline = 'middle'

  for (var t = 0; t < yRange.ticks.length; t++) {
    var y = pad.top + plotH - (yRange.ticks[t] / yRange.max) * plotH
    ctx.beginPath()
    ctx.moveTo(pad.left, y)
    ctx.lineTo(w - pad.right, y)
    ctx.stroke()
    ctx.fillText(String(yRange.ticks[t]), pad.left - 8, y)
  }

  // X 轴标签
  ctx.fillStyle = labelColor
  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'

  var barCount = values.length
  var totalGap = plotW * 0.3
  var barW = (plotW - totalGap) / barCount
  var gap = totalGap / (barCount + 1)

  for (var i = 0; i < barCount; i++) {
    var bx = pad.left + gap + i * (barW + gap)
    var barH = (values[i] / yRange.max) * plotH
    var by = pad.top + plotH - barH

    // 渐变柱体
    var gradient = ctx.createLinearGradient(bx, by, bx, pad.top + plotH)
    gradient.addColorStop(0, barColor)
    gradient.addColorStop(1, barColor + '60')

    // 绘制圆角矩形柱子
    var radius = Math.min(6, barW / 2)
    ctx.beginPath()
    ctx.moveTo(bx + radius, by)
    ctx.lineTo(bx + barW - radius, by)
    ctx.arcTo(bx + barW, by, bx + barW, by + radius, radius)
    ctx.lineTo(bx + barW, pad.top + plotH)
    ctx.lineTo(bx, pad.top + plotH)
    ctx.lineTo(bx, by + radius)
    ctx.arcTo(bx, by, bx + radius, by, radius)
    ctx.closePath()
    ctx.fillStyle = gradient
    ctx.fill()

    // X 轴标签
    ctx.fillText(labels[i], bx + barW / 2, h - pad.bottom + 10)

    // 数值标签
    ctx.fillStyle = '#333'
    ctx.font = '10px sans-serif'
    ctx.textBaseline = 'bottom'
    ctx.fillText(String(values[i]), bx + barW / 2, by - 4)
    ctx.fillStyle = labelColor
    ctx.font = '11px sans-serif'
    ctx.textBaseline = 'top'
  }
}

/**
 * 绘制饼图
 * @param {CanvasRenderingContext2D} ctx
 * @param {object} options
 *   labels: string[]
 *   values: number[]
 *   width: number
 *   height: number
 *   centerX / centerY: 可选，圆心
 *   radius: 可选，半径
 */
function drawPie(ctx, options) {
  var labels = options.labels || []
  var values = options.values || []
  var w = options.width
  var h = options.height
  var cx = options.centerX || w / 2
  var cy = options.centerY || h / 2 - 10
  var radius = options.radius || Math.min(w, h) / 2 - 30

  var colors = options.colors || [
    '#C97E5A', '#8B5A3C', '#D4A574', '#A0522D',
    '#E6C9A8', '#F0D5C0', '#B8733D', '#E8A87C',
    '#C4906A', '#9C6B4A', '#DFBFA0', '#B07D5B'
  ]

  if (labels.length === 0 || values.length === 0) {
    ctx.fillStyle = '#ccc'
    ctx.font = '14px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', w / 2, h / 2)
    return
  }

  var total = 0
  for (var i = 0; i < values.length; i++) {
    total += Math.max(0, values[i])
  }

  if (total === 0) {
    ctx.fillStyle = '#ccc'
    ctx.font = '14px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('暂无数据', w / 2, h / 2)
    return
  }

  var startAngle = -Math.PI / 2

  for (var j = 0; j < values.length; j++) {
    var val = Math.max(0, values[j])
    var sliceAngle = (val / total) * Math.PI * 2
    var endAngle = startAngle + sliceAngle

    // 饼图扇区
    ctx.beginPath()
    ctx.moveTo(cx, cy)
    ctx.arc(cx, cy, radius, startAngle, endAngle)
    ctx.closePath()
    ctx.fillStyle = colors[j % colors.length]
    ctx.fill()

    // 标签线 + 文字
    var midAngle = startAngle + sliceAngle / 2
    var labelR = radius + 18
    var lx = cx + Math.cos(midAngle) * labelR
    var ly = cy + Math.sin(midAngle) * labelR

    ctx.fillStyle = '#333'
    ctx.font = '10px sans-serif'
    ctx.textAlign = midAngle < -Math.PI / 2 || midAngle > Math.PI / 2 ? 'right' : 'left'
    ctx.textBaseline = 'middle'

    // 简短标签
    var shortLabel = labels[j]
    if (shortLabel.length > 5) shortLabel = shortLabel.substring(0, 5) + '..'
    var pct = Math.round(val / total * 100)
    var labelText = shortLabel + ' ' + pct + '%'
    ctx.fillText(labelText, lx, ly)

    startAngle = endAngle
  }
}

module.exports = {
  initCanvas: initCanvas,
  drawLine: drawLine,
  drawBar: drawBar,
  drawPie: drawPie
}
