import request from '@/utils/request'

export const userApi = {
  login: (data) => {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  register: (data) => {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  logout: () => {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  getProfile: () => {
    return request({
      url: '/user/profile',
      method: 'get'
    })
  },

  updateProfile: (data) => {
    return request({
      url: '/user/profile',
      method: 'put',
      data
    })
  },

  changePassword: (data) => {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  },

  getMyVehicles: () => {
    return request({
      url: '/user/vehicles',
      method: 'get'
    })
  },

  addVehicle: (data) => {
    return request({
      url: '/user/vehicles',
      method: 'post',
      data
    })
  },

  updateVehicle: (id, data) => {
    return request({
      url: `/user/vehicles/${id}`,
      method: 'put',
      data
    })
  },

  deleteVehicle: (id) => {
    return request({
      url: `/user/vehicles/${id}`,
      method: 'delete'
    })
  }
}
