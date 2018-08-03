import {AlertController, NavParams} from "ionic-angular";
import {NavController} from "ionic-angular";
import {Component} from "@angular/core";

import {TournamentController} from "../../controllers/tournamentController";
import {Match} from "../../models/tournament";
import {Team} from "../../models/tournament";

enum FinishOptions {
  Finish,
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

  constructor(
    readonly navCtrl: NavController,
    private readonly navParams: NavParams,
    private readonly alertCtrl: AlertController,
    private readonly tournamentCtrl: TournamentController,
  ) {
    this.teamPositions = {};
    this.leftWins = 0;
    this.rightWins = 0;
    const id = this.navParams.get("id");
    this.tournamentCtrl.setId(id);
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

  getSkill(team: Team) {
    if (!this.match) {
      return 0;
    }

    return Math.round((team.player1.trueSkillMean - 3 * team.player1.trueSkillStandardDeviation) +
                      (team.player2.trueSkillMean - 3 * team.player2.trueSkillStandardDeviation));
  }

  cancelMatch() {
    this.tournamentCtrl.cancelMatch().toPromise()
      .then(() => this.navCtrl.pop())
      .then(() => this.update());
  }

  private finishMatch(matchWinner: Team) {
    const [player1, player2] = [matchWinner.player1, matchWinner.player2];
    return this.tournamentCtrl.finishMatch()
      .then((tournamentFinished) => {
        if (tournamentFinished) {
          const title = "Done";
          const message = [player1.name, 'and', player2.name, 'won!'].join(' ');
          return this.tournamentCtrl.getBestOfN()
            .then((bestOfN) => {
              return this.showPlayBestOfNAlert(title, message)
                .then((finishOption) => {
                    switch (finishOption) {
                      case FinishOptions.Finish:
                        this.navCtrl.pop();
                        return this.tournamentCtrl.finishTournament();
                      case FinishOptions.Rematch:
                        return this.tournamentCtrl.finishTournament()
                          .then((oldTournament) => {
                            this.navCtrl.pop();
                            return this.tournamentCtrl.newTournament(oldTournament);
                          });
                    }
                  }
                )
            });
        }
        this.update();
      });
  }

  showPlayBestOfNAlert(title, message) {
    return new Promise((resolve, reject) => {
      const alert = this.alertCtrl.create({
        title,
        message,
        buttons: [{
          text: 'Finish',
          role: 'cancel',
          handler: () => {
            resolve(FinishOptions.Finish)
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
