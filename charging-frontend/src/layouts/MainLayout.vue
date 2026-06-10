<template>
  <el-container class="main-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="32" color="#409eff">
          <Lightning />
        </el-icon>
        <span class="logo-text">充电管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        class="sidebar-menu"
        router
        background-color="transparent"
        text-color="#303133"
        active-text-color="#409eff"
      >
        <el-menu-item
          v-for="route in menuRoutes"
          :key="route.path"
          :index="'/' + route.path"
        >
          <el-icon>
            <component :is="route.meta.icon" />
          </el-icon>
          <span>{{ route.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ currentUser?.username?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <div class="user-name">{{ currentUser?.username }}</div>
                <div class="user-balance">余额: ¥{{ currentUser?.balance?.toFixed(2) }}</div>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentUser = computed(() => userStore.currentUser)

const menuRoutes = computed(() => {
  return route.matched[0]?.children?.filter(r => !r.hidden) || []
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      if (currentUser.value?.id) {
        await userApi.logout(currentUser.value.id)
      }
      userStore.setCurrentUser(null)
      localStorage.removeItem('userId')
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      if (e !== 'cancel') {
        console.error(e)
      }
    }
  }
}

onMounted(async () => {
  try {
    const list = await userApi.list()
    userStore.setUserList(list)
  } catch (e) {
    console.error('获取用户列表失败', e)
  }
})
</script>

<style lang="scss" scoped>
.main-container {
  height: 100%;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #e4e7ed;

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  padding: 20px 0;

  .el-menu-item {
    height: 50px;
    line-height: 50px;
    margin: 4px 10px;
    border-radius: 8px;

    &:hover {
      background-color: #ecf5ff;
    }

    &.is-active {
      background-color: #ecf5ff;
      color: #409eff;
    }
  }
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;

  &:hover {
    background-color: #f5f7fa;
  }

  .arrow-icon {
    color: #909399;
    font-size: 12px;
  }
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-detail {
  .user-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }

  .user-balance {
    font-size: 12px;
    color: #67c23a;
  }
}

.main-content {
  background: #f5f7fa;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
