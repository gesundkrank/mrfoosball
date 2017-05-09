import {AlertController} from "ionic-angular";
import {NavController} from "ionic-angular";
import {Component} from "@angular/core";

import {TournamentController} from "../../controllers/tournamentController";
import {Match} from "../../models/tournament";
import {Team} from "../../models/tournament";

enum FinishOptions {
  Finish,
  PlayBestOfN,
  Rematch
}

@Component({
             selector: 'page-match',
             templateUrl: 'match.html'
           })
export class MatchPage {

  private match: Match;
  private leftTeam: Team;
  private rightTeam: Team;
  private leftWins: number;
  private rightWins: number;
  private teamPositions: { [key: string]: string };

  private bestOfN: number;

  constructor(readonly navCtrl: NavController,
              private readonly alertCtrl: AlertController,
              private readonly tournamentCtrl: TournamentController) {
    this.teamPositions = {};
    this.leftWins = 0;
    this.rightWins = 0;
  }

  ionViewDidEnter() {
    this.update();
  }

  addGoal(color: string) {
    this.tournamentCtrl
      .addGoal(this.teamPositions[color])
      .then(() => this.update());
  }

  undo() {
    this.tournamentCtrl.undo();
    this.update();
  }

  canUndo() {
    return this.tournamentCtrl.canUndo();
  }

  getUpdateInProgress() {
    return this.tournamentCtrl.getUpdateInProgress();
  }

  getScore(color: string) {
    if (!this.match) {
      return 0;
    }

    return this.match[this.teamPositions[color]];
  }

  cancelMatch() {
    this.tournamentCtrl.cancelMatch()
      .then(() => this.navCtrl.pop())
      .then(() => this.update());
  }

  private finishMatch(matchWinner: Team) {
    const [player1, player2] = [matchWinner.player1, matchWinner.player2];
    return this.tournamentCtrl.finishMatch()
      .then((tournamentFinished) => {
        if (tournamentFinished) {
          const title = ['Team', matchWinner, 'is the winner!'].join(' ');
          const message = [player1.name, 'and', player2.name, 'won!'].join(' ');
          return this.tournamentCtrl.getBestOfN()
            .then((bestOfN) => {
              this.showPlayBestOfNAlert(title, message, bestOfN)
                .then((finishOption) => {
                        switch (finishOption) {
                          case FinishOptions.PlayBestOfN:
                            this.tournamentCtrl.incrementBestOfN()
                              .then(() => this.tournamentCtrl.newMatch())
                              .then(() => this.update());
                            break;
                          case FinishOptions.Finish:
                            this.navCtrl.pop();
                            this.tournamentCtrl.finishTournament();
                            break;
                          case FinishOptions.Rematch:
                            this.tournamentCtrl.finishTournament()
                              .then(() => this.tournamentCtrl.newTournament());
                            this.navCtrl.pop();
                        }
                      }
                )
            });
        }
        this.update();
      });
  }

  showPlayBestOfNAlert(title, message, bestOfN) {
    return new Promise((resolve, reject) => {
      const alert = this.alertCtrl.create(
        {
          title,
          message,
          buttons: [{
            text: 'Finish',
            role: 'cancel',
            handler: () => {
              resolve(FinishOptions.Finish)
            },
          }, {
            text: 'Play best of ' + (bestOfN + 2),
            handler: () => {
              resolve(FinishOptions.PlayBestOfN)
            },
          }, {
            text: 'Rematch',
            handler: () => {
              resolve(FinishOptions.Rematch)
            },
          }],
        });
      alert.present();
    });
  }

  private getRunning() {
    return this.tournamentCtrl.getRunningMatch()
      .then(running => this.match = running);
  }

  private update() {
    this.getRunning()
      .then(() => this.tournamentCtrl.getWinnerTeam(this.match))
      .then((winner) => winner ? this.finishMatch(winner).then(() => this.getRunning()) : null)
      .then(() => this.tournamentCtrl.getTeamNames())
      .then((teamColors => this.teamPositions = teamColors))
      .then(() => this.tournamentCtrl.getTeams())
      .then((teams => {
        this.leftTeam = teams[this.teamPositions['left']];
        this.rightTeam = teams[this.teamPositions['right']];
      }))
      .then(() => this.tournamentCtrl.getWinnerTeam(this.match))
      .then((winner) => winner ? this.finishMatch(winner) : null)
      .then(() => this.tournamentCtrl.getWins())
      .then((wins) => {
        this.leftWins = wins[this.teamPositions['left']];
        this.rightWins = wins[this.teamPositions['right']];
      })
      .then(() => this.tournamentCtrl.getBestOfN())
      .then((bestOfN) => this.bestOfN = bestOfN);
  }
}
