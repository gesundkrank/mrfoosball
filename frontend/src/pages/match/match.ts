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

  teamA: Team;
  teamB: Team;
  match: Match;
  alert: any;
  wins: any;

  constructor(
    readonly navCtrl: NavController,
    private readonly alertCtrl: AlertController,
    private readonly toastCtrl: ToastController,
    private readonly tournament: Tournament,
  ) {
    //
  }

  ionViewDidEnter() {
    this.update();
  }

  addGoal(color: string) {
    let oldState: string;
    this.tournament.addGoal(this.getTeamNameForColor(color))
      .then((state) => {
        oldState = state;
        return this.update();
      })
      .then(() => this.showToast('Team: ' + color + ' scored!', 'UNDO'))
      .then((undone) => undone ? this.tournament.reset(oldState) : null)
      .then(() => this.update());
  }

  getPlayers(color: string) {
    if (this.teamA === undefined && this.teamB === undefined) {
      return [];
    }
    return this[this.getTeamNameForColor(color)].players;
  }

  getScore(color: string) {
    if (this.match === undefined) {
      return 0;
    }
    return this.match[this.getTeamNameForColor(color)];
  }

  getWins(color: string) {
    if (this.wins === undefined) {
      return 0;
    }
    return this.wins[this.getTeamNameForColor(color)];
  }

  cancelMatch() {
    let oldState: string;
    this.tournament.cancelMatch()
      .then((state) => {
        this.navCtrl.pop();
        oldState = state
      })
      .then(() => this.showToast('Match canceled!', 'UNDO'))
      .then((undone) => {
        if (undone) {
          this.tournament.reset(oldState);
          this.navCtrl.push(MatchPage);
        }
      })
      .then(() => this.update());
  }

  swapTeams() {
    this.tournament.swapTeams()
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
          const message = [
            'Tournament ended,', playerA.name, 'and', playerB.name, 'won!',
          ].join(' ');
          return this.tournament.getBestOfN()
            .then((bestOfN) => this.showToast(
              message,
              'Play best of ' + bestOfN + '?',
            ));
        }
        return true;
      })
      .then((newMatch) => {
        if (newMatch) {
          this.tournament.newMatch();
//            .then(() =>this.navCtrl.push(MatchPage));
        } else {
          this.tournament.finishTournament();
        }
      });
  }

  private getRunning() {
    return this.tournament.getRunningMatch()
      .then(running => this.match = running);
  }

  private getTeamNameForColor(color: string) {
    const matchCount = this.wins ? this.wins.teamA + this.wins.teamB : 0;
    if (matchCount % 2 == 0) {
      return {
        grey: 'teamA',
        black: 'teamB',
      }[color];
    }
    return {
      grey: 'teamB',
      black: 'teamA',
    }[color];
  }

  private update() {
    return this.tournament.getTeams()
      .then(([teamA, teamB]) => [this.teamA, this.teamB] = [teamA, teamB])
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
      duration: 6000,
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
