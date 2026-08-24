<template>
  <Login v-if="!loggedIn" @login-success="handleLoginSuccess" />
  <MainLayout v-else :dept-info="deptInfo" @logout="handleLogout" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Login from './components/Login.vue'
import MainLayout from './components/MainLayout.vue'

const LOGIN_KEY = 'loginTime'
const DEPT_KEY = 'deptInfo'
const EXPIRE_MS = 2 * 60 * 60 * 1000 // 2 小时

const loggedIn = ref(false)
const deptInfo = ref(null)

const handleLoginSuccess = (info) => {
  localStorage.setItem(LOGIN_KEY, String(Date.now()))
  localStorage.setItem(DEPT_KEY, JSON.stringify(info || {}))
  deptInfo.value = info || {}
  loggedIn.value = true
}

const handleLogout = () => {
  localStorage.removeItem(LOGIN_KEY)
  localStorage.removeItem(DEPT_KEY)
  deptInfo.value = null
  loggedIn.value = false
}

onMounted(() => {
  const stored = localStorage.getItem(LOGIN_KEY)
  if (stored) {
    const elapsed = Date.now() - Number(stored)
    if (elapsed < EXPIRE_MS) {
      loggedIn.value = true
      try {
        deptInfo.value = JSON.parse(localStorage.getItem(DEPT_KEY) || '{}')
      } catch (e) {
        deptInfo.value = {}
      }
    } else {
      localStorage.removeItem(LOGIN_KEY)
      localStorage.removeItem(DEPT_KEY)
    }
  }
})
</script>
