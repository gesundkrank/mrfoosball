import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';

import { MatchPage } from '../../pages/match/match';

@Component({
  selector: 'page-stats',
  templateUrl: 'stats.html'
})
export class StatsPage {

  constructor(
    public navCtrl: NavController,
  ) {
    //
  }

  newMatch() {
    this.navCtrl.push(MatchPage);
  }

}
