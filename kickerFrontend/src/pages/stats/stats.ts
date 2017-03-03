import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';

import { MatchPage } from '../../pages/match/match';
import { Tournament } from '../../providers/tournament';

@Component({
  selector: 'page-stats',
  templateUrl: 'stats.html'
})
export class StatsPage {

  constructor(
    private readonly navCtrl: NavController,
    private readonly tournament: Tournament,
  ) {
    //
  }

  ionViewDidEnter() {
    this.tournament.getRunningMatch()
      .then((match) => {
        if (match) {
          this.newMatch();
        }
      })
  }

  newMatch() {
    this.navCtrl.push(MatchPage);
  }

}
