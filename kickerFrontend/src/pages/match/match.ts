import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { ToastController } from 'ionic-angular';

import { Tournament } from '../../providers/tournament';
import { Team } from '../../providers/tournament';
import { Match } from '../../providers/tournament';


@Component({
  selector: 'page-match',
  templateUrl: 'match.html'
})
export class MatchPage {

  teamA: Team;
  teamB: Team;
  match: Match;

  constructor(
    public navCtrl: NavController,
    private toastCtrl: ToastController,
    readonly tournament: Tournament,
  ) {
    this.update();
  }

  addGoal(team: string) {
    this.tournament.addGoal(team)
      .then((previousState) => {
        let toast = this.toastCtrl.create({
          message: 'Team: ' + team + ' scored!',
          duration: 3000,
          position: 'top',
          showCloseButton: true,
          closeButtonText: 'UNDO',
        });

        toast.onDidDismiss((_data, role) => {
          // If user clicks the 'UNDO' button, the role is 'close',
          // otherwise it is 'backdrop', which means he does not
          // want to 'undo'.
          if (role === 'close') {
            this.tournament.reset(previousState)
              .then(() => this.update());
          }
        });
        toast.present();
      })
      .then(() => this.update());
  }

  private update() {
    return Promise.resolve()
      .then(() => this.tournament.getTeams())
      .then(([teamA, teamB]) => {
        this.teamA = teamA;
        this.teamB = teamB;
      })
      .then(() => this.tournament.getRunningMatch())
      .then(match => {
        this.match = match;
      });
  }

}
