import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/station',
    children: [
      {
        path: 'station',
        name: 'Station',
        component: () => import('@/views/StationList.vue'),
        meta: { title: '充电站点', icon: 'Location' }
      },
      {
        path: 'station/:id',
        name: 'StationDetail',
        component: () => import('@/views/StationDetail.vue'),
        meta: { title: '站点详情', icon: 'Location' },
        hidden: true
      },
      {
        path: 'reservation',
        name: 'Reservation',
        component: () => import('@/views/ReservationList.vue'),
        meta: { title: '预约管理', icon: 'Calendar' }
      },
      {
        path: 'queue',
        name: 'Queue',
        component: () => import('@/views/QueueList.vue'),
        meta: { title: '排队叫号', icon: 'List' }
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('@/views/RecordList.vue'),
        meta: { title: '充电记录', icon: 'Document' }
      },
      {
        path: 'fee',
        name: 'Fee',
        component: () => import('@/views/FeeList.vue'),
        meta: { title: '费用管理', icon: 'Money' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - 新能源汽车充电排队与预约系统'
  }
  next()
})

export default router
