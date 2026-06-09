import request from '@/utils/request'

export const recordApi = {
  getList: (params) => {
    return request({
      url: '/charging-records',
      method: 'get',
      params
    })
  },

  getDetail: (id) => {
    return request({
      url: `/charging-records/${id}`,
      method: 'get'
    })
  },

  startCharging: (pileId, data) => {
    return request({
      url: `/piles/${pileId}/start`,
      method: 'post',
      data
    })
  },

  endCharging: (recordId) => {
    return request({
      url: `/charging-records/${recordId}/end`,
      method: 'put'
    })
  },

  getChargingStatus: (recordId) => {
    return request({
      url: `/charging-records/${recordId}/status`,
      method: 'get'
    })
  },

  getStatistics: (params) => {
    return request({
      url: '/charging-records/statistics',
      method: 'get',
      params
    })
  }
}
