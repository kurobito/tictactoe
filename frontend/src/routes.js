import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

const router = new Router({
  mode: "history", // Use browser history
  routes: [
    {
      path: "/",
      name: "Home",
      component: () => import("./components/Grid")
    },
    {
      path: "/game",
      name: "Courses",
      component: () => import("./components/Grid")
    }
  ]
});

export default router;
