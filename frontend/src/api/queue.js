import request from '@/utils/request'

export const queueApi = {
  getCurrentNumber: (stationId) => {
    return request({
      url: `/stations/${stationId}/queue/current`,
      method: 'get'
    })
  },

  getMyQueueStatus: (stationId) => {
    return request({
      url: `/stations/${stationId}/queue/my`,
      method: 'get'
    })
  },

  joinQueue: (stationId, data) => {
    return request({
      url: `/stations/${stationId}/queue/join`,
      method: 'post',
      data
    })
  },

  leaveQueue: (stationId) => {
    return request({
      url: `/stations/${stationId}/queue/leave`,
      method: 'delete'
    })
  },

  getQueueList: (stationId) => {
    return request({
      url: `/stations/${stationId}/queue`,
      method: 'get'
    })
  },

  callNext: (stationId) => {
    return request({
      url: `/stations/${stationId}/queue/next`,
      method: 'post'
    })
  }
}
