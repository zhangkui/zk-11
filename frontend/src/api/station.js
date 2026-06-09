import request from '@/utils/request'

export const stationApi = {
  getList: (params) => {
    return request({
      url: '/stations',
      method: 'get',
      params
    })
  },

  getDetail: (id) => {
    return request({
      url: `/stations/${id}`,
      method: 'get'
    })
  },

  add: (data) => {
    return request({
      url: '/stations',
      method: 'post',
      data
    })
  },

  update: (id, data) => {
    return request({
      url: `/stations/${id}`,
      method: 'put',
      data
    })
  },

  delete: (id) => {
    return request({
      url: `/stations/${id}`,
      method: 'delete'
    })
  },

  getChargingPiles: (stationId, params) => {
    return request({
      url: `/stations/${stationId}/piles`,
      method: 'get',
      params
    })
  },

  addChargingPile: (stationId, data) => {
    return request({
      url: `/stations/${stationId}/piles`,
      method: 'post',
      data
    })
  },

  updateChargingPile: (pileId, data) => {
    return request({
      url: `/piles/${pileId}`,
      method: 'put',
      data
    })
  },

  deleteChargingPile: (pileId) => {
    return request({
      url: `/piles/${pileId}`,
      method: 'delete'
    })
  }
}
