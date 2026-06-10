import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)

  const userList = ref([])

  const userId = computed(() => currentUser.value?.id)

  const userRole = computed(() => currentUser.value?.role || parseInt(localStorage.getItem('userRole')) || 0)

  const isAdmin = computed(() => userRole.value === 2)

  const isLoggedIn = computed(() => currentUser.value !== null)

  const setCurrentUser = (user) => {
    currentUser.value = user
    if (user?.role) {
      localStorage.setItem('userRole', user.role)
    }
  }

  const setUserList = (list) => {
    userList.value = list
  }

  const updateBalance = (balance) => {
    if (currentUser.value) {
      currentUser.value.balance = balance
    }
  }

  const logout = () => {
    currentUser.value = null
    userList.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
  }

  return {
    currentUser,
    userList,
    userId,
    userRole,
    isAdmin,
    isLoggedIn,
    setCurrentUser,
    setUserList,
    updateBalance,
    logout
  }
})
