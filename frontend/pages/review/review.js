// pages/review/review.js
const { post } = require('../../utils/request')

Page({
  data: {
    orderId: '',
    rating: 5,
    foodRating: 5,
    serviceRating: 5,
    environmentRating: 5,
    catInteractionRating: 5,
    content: '',
    tagList: [
      { label: '猫咪可爱', selected: false },
      { label: '环境舒适', selected: false },
      { label: '服务贴心', selected: false },
      { label: '菜品美味', selected: false },
      { label: '性价比高', selected: false },
      { label: '下次还来', selected: false }
    ],
    submitting: false
  },
  onLoad(options) {
    this.setData({ orderId: options.orderId || '' })
  },
  onRatingChange(e) {
    this.setData({ rating: e.currentTarget.dataset.value })
  },
  onFoodRatingChange(e) {
    this.setData({ foodRating: e.currentTarget.dataset.value })
  },
  onServiceRatingChange(e) {
    this.setData({ serviceRating: e.currentTarget.dataset.value })
  },
  onEnvironmentRatingChange(e) {
    this.setData({ environmentRating: e.currentTarget.dataset.value })
  },
  onCatRatingChange(e) {
    this.setData({ catInteractionRating: e.currentTarget.dataset.value })
  },
  onTagClick(e) {
    const idx = e.currentTarget.dataset.index
    const tagList = this.data.tagList
    tagList[idx].selected = !tagList[idx].selected
    this.setData({ tagList: [...tagList] })
  },
  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },
  onSubmit() {
    if (!this.data.tagList.some(t => t.selected)) {
      wx.showToast({ title: '请至少选择一个标签', icon: 'none' }); return
    }
    this.setData({ submitting: true })
    const selectedTags = this.data.tagList.filter(t => t.selected).map(t => t.label)
    post('/api/review/submit', {
      orderId: this.data.orderId,
      rating: this.data.rating,
      foodRating: this.data.foodRating,
      serviceRating: this.data.serviceRating,
      environmentRating: this.data.environmentRating,
      catInteractionRating: this.data.catInteractionRating,
      tags: selectedTags,
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
