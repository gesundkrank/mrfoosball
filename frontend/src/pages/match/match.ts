import { AlertController } from 'ionic-angular';
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

  teamGrey: Team;
  teamBlack: Team;
  match: Match;
  alert: any;
  wins: any;

  constructor(
    private readonly navCtrl: NavController,
    private readonly alertCtrl: AlertController,
    private readonly toastCtrl: ToastController,
    private readonly tournament: Tournament,
  ) {
    //
  }
  ionViewDidEnter() {
    this.update();
  }

  addGoal(team: string) {
    let oldState: string;
    this.tournament.addGoal(team)
      .then((state) => {
        oldState = state;
        return this.update();
      })
      .then(() => this.showToast('Team: ' + team + ' scored!', 'UNDO'))
      .then((undone) => undone ? this.tournament.reset(oldState) : null)
      .then(() => this.update());
  }

  private finishMatch(matchWinner: Team) {
    const [playerA, playerB] = matchWinner.players;
    return this.tournament.finishMatch()
      .then((args) => {
        const match = args[0] as Match;
        const tournamentFinished = args[1];
        this.match = match;
        this.navCtrl.pop();
        if (tournamentFinished) {
          return this.tournament.getBestOfN()
            .then((bestOfN) => this.showToast(
              'Tournament ended, ' + playerA + ' and ' + playerB + 'have won!',
              'Play best of ' + bestOfN + '?',
            ));
        }
        return true;
      })
      .then((newMatch) => {
        if (newMatch) {
          this.tournament.newMatch()
            .then(() =>this.navCtrl.push(MatchPage));
        }
      });
  }

  private getRunning() {
    return this.tournament.getRunningMatch()
      .then(running => this.match = running);
  }

  private update() {
    return this.tournament.getTeams()
      .then(([teamGrey, teamBlack]) => {
        this.teamGrey = teamGrey;
        this.teamBlack = teamBlack;
      })
      .then(() => this.getRunning())
      .then((running) => this.tournament.getWinner(running))
      .then((winner) => winner ? this.finishMatch(winner) : null)
      .then(() => this.getRunning())
      .then((running) => this.match = running)
      .then(() => this.tournament.getWins())
      .then((wins) => this.wins = wins);
  }

  private showToast(message: string, text: string) {
    let toast = this.toastCtrl.create({
      message,
      duration: 3000,
      position: 'top',
      showCloseButton: true,
      closeButtonText: text,
    });
    return new Promise<boolean>(resolve => {
      toast.onDidDismiss((_data, role) => {
        // If user clicks the 'UNDO' button, the role is 'close',
        // otherwise it is 'backdrop', which means he does not
        // want to 'undo'.
        if (role !== 'close') {
          resolve(false);
          return;
        }
        resolve(true);
      });
      toast.present();
    });
  }

}
