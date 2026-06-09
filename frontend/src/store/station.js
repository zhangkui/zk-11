import { defineStore } from 'pinia'
import { ref } from 'vue'
import { stationApi } from '@/api/station'

export const useStationStore = defineStore('station', () => {
  const stations = ref([])
  const currentStation = ref(null)
  const chargingPiles = ref([])
  const loading = ref(false)

  const fetchStations = async (params) => {
    loading.value = true
    try {
      const res = await stationApi.getList(params)
      stations.value = res.data.list || res.data || []
      return res
    } finally {
      loading.value = false
    }
  }

  const fetchStationDetail = async (id) => {
    loading.value = true
    try {
      const res = await stationApi.getDetail(id)
      currentStation.value = res.data
      return res
    } finally {
      loading.value = false
    }
  }

  const fetchChargingPiles = async (stationId) => {
    loading.value = true
    try {
      const res = await stationApi.getChargingPiles(stationId)
      chargingPiles.value = res.data.list || res.data || []
      return res
    } finally {
      loading.value = false
    }
  }

  const addStation = async (data) => {
    const res = await stationApi.add(data)
    await fetchStations()
    return res
  }

  const updateStation = async (id, data) => {
    const res = await stationApi.update(id, data)
    await fetchStations()
    return res
  }

  const deleteStation = async (id) => {
    const res = await stationApi.delete(id)
    await fetchStations()
    return res
  }

  const addChargingPile = async (stationId, data) => {
    const res = await stationApi.addChargingPile(stationId, data)
    await fetchChargingPiles(stationId)
    return res
  }

  const updateChargingPile = async (pileId, data) => {
    const res = await stationApi.updateChargingPile(pileId, data)
    if (currentStation.value) {
      await fetchChargingPiles(currentStation.value.id)
    }
    return res
  }

  const deleteChargingPile = async (pileId) => {
    const res = await stationApi.deleteChargingPile(pileId)
    if (currentStation.value) {
      await fetchChargingPiles(currentStation.value.id)
    }
    return res
  }

  return {
    stations,
    currentStation,
    chargingPiles,
    loading,
    fetchStations,
    fetchStationDetail,
    fetchChargingPiles,
    addStation,
    updateStation,
    deleteStation,
    addChargingPile,
    updateChargingPile,
    deleteChargingPile
  }
})
