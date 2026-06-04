// pages/review/review.js
const { post } = require('../../utils/request')

Page({
  data: {
    orderId: '',
    rating: 5,
    content: '',
    tags: ['猫咪可爱', '环境舒适', '服务贴心', '菜品美味', '性价比高', '下次还来'],
    selectedTags: [],
    submitting: false
  },
  onLoad(options) {
    this.setData({ orderId: options.orderId || '' })
  },
  onRatingChange(e) {
    this.setData({ rating: e.detail.value })
  },
  onTagClick(e) {
    const tag = e.currentTarget.dataset.tag
    const selected = this.data.selectedTags
    const idx = selected.indexOf(tag)
    if (idx >= 0) selected.splice(idx, 1)
    else selected.push(tag)
    this.setData({ selectedTags: [...selected] })
  },
  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },
  onSubmit() {
    if (this.data.selectedTags.length === 0) {
      wx.showToast({ title: '请至少选择一个标签', icon: 'none' }); return
    }
    this.setData({ submitting: true })
    post('/api/review/submit', {
      orderId: this.data.orderId,
      rating: this.data.rating,
      tags: this.data.selectedTags,
      content: this.data.content
    }).then(res => {
      this.setData({ submitting: false })
      if (res.code === 0) {
        wx.showToast({ title: `评价成功！+${res.data.pointsEarned}积分`, icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1200)
      }
    }).catch(() => this.setData({ submitting: false }))
  }
})
