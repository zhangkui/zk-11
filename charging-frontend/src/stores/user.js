import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)

  const userList = ref([])

  const userId = computed(() => currentUser.value?.id)

  const isLoggedIn = computed(() => currentUser.value !== null)

  const setCurrentUser = (user) => {
    currentUser.value = user
  }

  const setUserList = (list) => {
    userList.value = list
  }

  const updateBalance = (balance) => {
    if (currentUser.value) {
      currentUser.value.balance = balance
    }
  }

  return {
    currentUser,
    userList,
    userId,
    isLoggedIn,
    setCurrentUser,
    setUserList,
    updateBalance
  }
})
