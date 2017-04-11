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

  private bestOfN: number;

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
    const team = this.getTeamNameForColor(color);
    this.tournament.addGoal(team)
      .then(() => this.update());
  }

  undo() {
    this.tournament.undo();
    this.update();
  }

  getUpdateInProgress() {
    return this.tournament.getUpdateInProgress();
  }

  getTeamName(color: string) {
    if (this.teamA === undefined || this.teamB === undefined) {
      return;
    }
    return this[this.getTeamNameForColor(color)].name;
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
    this.tournament.cancelMatch()
      .then(() => this.navCtrl.pop())
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
        if (tournamentFinished) {
          const title = [
            'Team', matchWinner.name, 'is the winner!'
          ].join(' ');
          const message = [
            playerA.name, 'and', playerB.name, 'won!',
          ].join(' ');
          return this.tournament.getBestOfN()
            .then((bestOfN) => this.showPlayBestOfNAlert(title, message, bestOfN));
        }
        return true;
      })
      .then((newMatch) => {
        if (newMatch) {
          this.tournament.newMatch();
        } else {
          this.navCtrl.pop();
          this.tournament.finishTournament();
        }
      });
  }

  showPlayBestOfNAlert(title, message, bestOfN) {
    return new Promise((resolve, reject) => {
      let alert = this.alertCtrl.create({
        title,
        message,
        buttons: [{
         text: 'Finish',
         role: 'cancel',
         handler: () => {resolve(false)},
        }, {
         text: 'Play best of ' + bestOfN,
         handler: () => {resolve(true)},
        }],
      });
      alert.present();
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
      .then((wins) => this.wins = wins)
      .then(() => this.tournament.getBestOfN())
      .then((bestOfN) => this.bestOfN = bestOfN);
  }
}
