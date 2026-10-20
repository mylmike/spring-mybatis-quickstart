<template>
  <Login v-if="!loggedIn" @login-success="handleLoginSuccess" />
  <MainLayout v-else :dept-info="deptInfo" :menu-tree="menuTree" @logout="handleLogout" />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Login from './components/Login.vue'
import MainLayout from './components/MainLayout.vue'
import { fetchUserPermission } from './permission.js'
import { fetchMenuTree } from './api/pendingOrders.js'

const LOGIN_KEY = 'loginTime'
const DEPT_KEY = 'deptInfo'
const USER_KEY = 'userAccount'
const EXPIRE_MS = 2 * 60 * 60 * 1000 // 2 小时

const loggedIn = ref(false)
const deptInfo = ref(null)
/** 后端返回的菜单树（gzweuc_t），传给 MainLayout 由它按权限二次过滤 */
const menuTree = ref([])

const loadMenuTree = async () => {
  try {
    const res = await fetchMenuTree()
    if (res.data?.success) {
      menuTree.value = res.data.tree || []
    } else {
      menuTree.value = []
    }
  } catch (e) {
    console.warn('获取菜单树失败:', e?.message || e)
    menuTree.value = []
  }
}

const handleLoginSuccess = async (info) => {
  // 登录成功后立即拉取权限明细 + 菜单树，再进入主界面
  await fetchUserPermission({ gzypuc001: info?.username || '' })
  await loadMenuTree()
  localStorage.setItem(LOGIN_KEY, String(Date.now()))
  localStorage.setItem(DEPT_KEY, JSON.stringify(info || {}))
  localStorage.setItem(USER_KEY, info?.username || '')
  deptInfo.value = info || {}
  loggedIn.value = true
}

const handleLogout = () => {
  localStorage.removeItem(LOGIN_KEY)
  localStorage.removeItem(DEPT_KEY)
  localStorage.removeItem(USER_KEY)
  deptInfo.value = null
  menuTree.value = []
  loggedIn.value = false
}

onMounted(async () => {
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
      // 刷新页面后按本地账号重新拉取权限 + 菜单
      const userAccount = localStorage.getItem(USER_KEY) || ''
      await fetchUserPermission({ gzypuc001: userAccount })
      await loadMenuTree()
    } else {
      localStorage.removeItem(LOGIN_KEY)
      localStorage.removeItem(DEPT_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
</script>
