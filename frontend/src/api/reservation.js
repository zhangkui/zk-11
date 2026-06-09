import request from '@/utils/request'

export const reservationApi = {
  getList: (params) => {
    return request({
      url: '/reservations',
      method: 'get',
      params
    })
  },

  getDetail: (id) => {
    return request({
      url: `/reservations/${id}`,
      method: 'get'
    })
  },

  create: (data) => {
    return request({
      url: '/reservations',
      method: 'post',
      data
    })
  },

  cancel: (id) => {
    return request({
      url: `/reservations/${id}/cancel`,
      method: 'put'
    })
  },

  getTimeSlots: (stationId, date) => {
    return request({
      url: `/stations/${stationId}/time-slots`,
      method: 'get',
      params: { date }
    })
  }
}
