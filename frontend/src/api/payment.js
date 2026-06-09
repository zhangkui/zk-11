import request from '@/utils/request'

export const paymentApi = {
  getList: (params) => {
    return request({
      url: '/payments',
      method: 'get',
      params
    })
  },

  getDetail: (id) => {
    return request({
      url: `/payments/${id}`,
      method: 'get'
    })
  },

  create: (recordId, data) => {
    return request({
      url: `/charging-records/${recordId}/pay`,
      method: 'post',
      data
    })
  },

  getPaymentStatus: (paymentId) => {
    return request({
      url: `/payments/${paymentId}/status`,
      method: 'get'
    })
  }
}
