import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/aqiFeedback',
    },
    {
      path: '/aqiFeedback',
      name: 'aqiFeedback',
      component: () => import('../views/AqiFeedBackList.vue'),
    },
  ],
})

export default router
