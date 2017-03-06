import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import * as screenfull from 'screenfull';

import { StatsPage } from '../../pages/stats/stats';

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
    this.navCtrl.push(StatsPage);
  }

}
