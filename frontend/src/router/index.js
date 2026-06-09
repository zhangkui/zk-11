import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/station/:id',
    name: 'Station',
    component: () => import('@/views/Station.vue'),
    meta: { title: '站点详情' }
  },
  {
    path: '/reservation',
    name: 'Reservation',
    component: () => import('@/views/Reservation.vue'),
    meta: { title: '我的预约' }
  },
  {
    path: '/queue',
    name: 'Queue',
    component: () => import('@/views/Queue.vue'),
    meta: { title: '排队叫号' }
  },
  {
    path: '/record',
    name: 'Record',
    component: () => import('@/views/Record.vue'),
    meta: { title: '充电记录' }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/Payment.vue'),
    meta: { title: '支付记录' }
  },
  {
    path: '/station-manage',
    name: 'StationManage',
    component: () => import('@/views/StationManage.vue'),
    meta: { title: '站点管理' }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/User.vue'),
    meta: { title: '用户中心' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '充电预约系统'} - 新能源汽车充电排队与预约系统`
  next()
})

export default router
