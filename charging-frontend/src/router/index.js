import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
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
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - 新能源汽车充电排队与预约系统'
  }

  const userStore = useUserStore()
  const userId = localStorage.getItem('userId')

  if (to.meta.public) {
    if (userId && userStore.currentUser) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!userId) {
    next('/login')
    return
  }

  if (!userStore.currentUser) {
    try {
      const user = await userApi.getInfo(userId)
      userStore.setCurrentUser(user)
      next()
    } catch (e) {
      localStorage.removeItem('userId')
      next('/login')
    }
  } else {
    next()
  }
})

export default router
