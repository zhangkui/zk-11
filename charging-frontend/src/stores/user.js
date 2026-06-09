import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref({
    id: 1,
    username: '张三',
    phone: '13800138001',
    licensePlate: '京A12345',
    balance: 500.00,
    status: 1
  })

  const userList = ref([])

  const userId = computed(() => currentUser.value?.id)

  const setCurrentUser = (user) => {
    currentUser.value = user
  }

  const setUserList = (list) => {
    userList.value = list
  }

  return {
    currentUser,
    userList,
    userId,
    setCurrentUser,
    setUserList
  }
})
