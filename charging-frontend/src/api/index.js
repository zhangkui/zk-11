import request from '@/utils/request'

export const stationApi = {
  page: (params) => request.get('/station/page', { params }),
  getDetail: (id) => request.get(`/station/${id}`),
  save: (data) => request.post('/station/save', data),
  updateStatus: (id, status) => request.put(`/station/${id}/status?status=${status}`),
  delete: (id) => request.delete(`/station/${id}`)
}

export const reservationApi = {
  create: (data) => request.post('/reservation/create', data),
  cancel: (data) => request.post('/reservation/cancel', data),
  confirmArrive: (id) => request.put(`/reservation/${id}/arrive`),
  pageByUser: (userId, pageNum = 1, pageSize = 10) =>
    request.get(`/reservation/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`),
  getDetail: (id) => request.get(`/reservation/${id}`)
}

export const queueApi = {
  join: (data) => request.post('/queue/join', data),
  cancel: (id) => request.put(`/queue/${id}/cancel`),
  callNext: (stationId) => request.post(`/queue/call/${stationId}`),
  getInfo: (id) => request.get(`/queue/${id}`),
  pageByUser: (userId, pageNum = 1, pageSize = 10) =>
    request.get(`/queue/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`),
  getAheadCount: (stationId, queueNumber) =>
    request.get(`/queue/ahead/${stationId}/${queueNumber}`)
}

export const recordApi = {
  start: (data) => request.post('/record/start', data),
  end: (data) => request.post('/record/end', data),
  pageByUser: (userId, pageNum = 1, pageSize = 10) =>
    request.get(`/record/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`),
  getCurrent: (userId, stationId) =>
    request.get(`/record/current?userId=${userId}${stationId ? '&stationId=' + stationId : ''}`),
  getDetail: (id) => request.get(`/record/${id}`)
}

export const feeApi = {
  pay: (data) => request.post('/fee/pay', data),
  pageByUser: (userId, pageNum = 1, pageSize = 10) =>
    request.get(`/fee/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`),
  getByRecordId: (recordId) => request.get(`/fee/record/${recordId}`),
  getDetail: (id) => request.get(`/fee/${id}`)
}

export const userApi = {
  list: () => request.get('/user/list'),
  getDetail: (id) => request.get(`/user/${id}`),
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  getInfo: (id) => request.get(`/user/info/${id}`),
  updateInfo: (id, data) => request.put(`/user/${id}`, data),
  logout: (id) => request.post(`/user/logout/${id}`)
}

export const dashboardApi = {
  getStats: () => request.get('/dashboard/stats')
}
