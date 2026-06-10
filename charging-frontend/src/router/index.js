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
    redirect: '/station',
    children: [
      {
        path: 'station',
        name: 'Station',
        component: () => import('@/views/StationList.vue'),
        meta: { title: '充电站点', icon: 'Location', roles: [1, 2] }
      },
      {
        path: 'station/:id',
        name: 'StationDetail',
        component: () => import('@/views/StationDetail.vue'),
        meta: { title: '站点详情', icon: 'Location', roles: [1, 2] },
        hidden: true
      },
      {
        path: 'reservation',
        name: 'Reservation',
        component: () => import('@/views/ReservationList.vue'),
        meta: { title: '预约管理', icon: 'Calendar', roles: [1, 2] }
      },
      {
        path: 'queue',
        name: 'Queue',
        component: () => import('@/views/QueueList.vue'),
        meta: { title: '排队叫号', icon: 'List', roles: [1, 2] }
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('@/views/RecordList.vue'),
        meta: { title: '充电记录', icon: 'Document', roles: [1, 2] }
      },
      {
        path: 'fee',
        name: 'Fee',
        component: () => import('@/views/FeeList.vue'),
        meta: { title: '费用管理', icon: 'Money', roles: [1, 2] }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: [2] }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis', roles: [2] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', roles: [1, 2] }
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
  const token = localStorage.getItem('token')

  if (to.meta.public) {
    if (token && userStore.currentUser) {
      if (userStore.isAdmin) {
        next('/dashboard')
      } else {
        next('/station')
      }
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  if (!userStore.currentUser) {
    try {
      const user = await userApi.getInfo()
      userStore.setCurrentUser(user)
      checkRolePermission(to, userStore, next)
    } catch (e) {
      userStore.logout()
      next('/login')
    }
  } else {
    checkRolePermission(to, userStore, next)
  }
})

function checkRolePermission(to, userStore, next) {
  const roles = to.meta.roles
  if (roles && !roles.includes(userStore.userRole)) {
    if (userStore.isAdmin) {
      next('/dashboard')
    } else {
      next('/station')
    }
    return
  }
  next()
}

export default router
