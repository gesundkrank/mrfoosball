import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import * as screenfull from 'screenfull';


@Component({
  selector: 'page-start',
  templateUrl: 'start.html',
})
export class StartPage {

  constructor(
    public navCtrl: NavController,
  ) {
    //
  }

  start() {
    screenfull.request();
  }

}
