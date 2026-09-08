import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'systemEntry',
      component: () => import('../views/SystemEntry.vue'),
    },
    {
      path: '/neps/login',
      name: 'nepsLogin',
      component: () => import('../views/NepsLogin.vue'),
    },
    {
      path: '/aqiFeedback',
      name: 'aqiFeedback',
      component: () => import('../views/AqiFeedBackList.vue'),
    },
  ],
})

export default router
