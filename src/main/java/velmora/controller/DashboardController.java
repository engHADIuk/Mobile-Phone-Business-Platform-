package velmora.controller;

import velmora.SceneManager;

public class DashboardController {

    public void goToOrder()   { SceneManager.show("product"); }
    public void goToTrack()   { SceneManager.show("track"); }
    public void goToManage()  { SceneManager.show("manage"); }
    public void goToReturns() { SceneManager.show("returns"); }
    public void goToCart()    { SceneManager.show("cart"); }
}
