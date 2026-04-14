import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/articles'
    },
    {
      path: '/articles',
      name: 'articles',
      component: () => import('@/views/articles/index.vue')
    },
    {
      path: '/articles/create',
      name: 'article-create',
      component: () => import('@/views/articles/Edit.vue')
    },
    {
      path: '/articles/:id/edit',
      name: 'article-edit',
      component: () => import('@/views/articles/Edit.vue')
    },
    {
      path: '/platforms',
      name: 'platforms',
      component: () => import('@/views/platforms/index.vue')
    },
    {
      path: '/publish',
      name: 'publish',
      component: () => import('@/views/publish/index.vue')
    },
    {
      path: '/history',
      name: 'history',
      component: () => import('@/views/history/index.vue')
    }
  ]
})

export default router
