import dayjs from 'dayjs'

export const formatDateTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

export const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

export const formatTime = (date) => {
  if (!date) return '-'
  return dayjs(date).format('HH:mm:ss')
}

export const formatDuration = (minutes) => {
  if (!minutes) return '-'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
}

export const getStatusName = (type, status) => {
  const statusMap = {
    station: { 0: '禁用', 1: '启用' },
    pile: { 0: '空闲', 1: '使用中', 2: '预约中', 3: '故障', 4: '维护中' },
    reservation: { 0: '待确认', 1: '已确认', 2: '进行中', 3: '已完成', 4: '已取消', 5: '已超时' },
    queue: { 0: '排队中', 1: '叫号中', 2: '已使用', 3: '已取消', 4: '已超时' },
    charging: { 0: '充电中', 1: '已完成', 2: '异常终止' },
    pay: { 0: '待支付', 1: '已支付', 2: '已退款' },
    payMethod: { 1: '余额', 2: '微信', 3: '支付宝' },
    pileType: { 1: '快充', 2: '慢充' }
  }
  return statusMap[type]?.[status] || '未知'
}

export const getStatusClass = (type, status) => {
  const classMap = {
    station: { 0: 'danger', 1: 'success' },
    pile: { 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'info' },
    reservation: { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'info', 5: 'danger' },
    queue: { 0: 'primary', 1: 'success', 2: 'info', 3: 'info', 4: 'danger' },
    charging: { 0: 'success', 1: 'info', 2: 'danger' },
    pay: { 0: 'warning', 1: 'success', 2: 'info' }
  }
  return classMap[type]?.[status] || 'info'
}
