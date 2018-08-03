import {Component} from "@angular/core";
import {NavController, NavParams} from "ionic-angular";
import {MatchPage} from "../match/match";
import {TournamentController} from "../../controllers/tournamentController";
import {Player, Tournament} from "../../models/tournament";
import {PlayerSkill} from "../../models/playerSkill";
import {IndexPage} from "../index";
import {HttpClient} from "@angular/common/http";

@Component({
             selector: 'page-stats',
             templateUrl: 'stats.html'
           })
export class StatsPage {

  id: string;
  queue: Array<Player>;
  stats: Array<PlayerSkill>;
  lastTournaments: Array<Tournament>;

  constructor(public http: HttpClient,
              private readonly navCtrl: NavController,
              private readonly navParams: NavParams,
              private readonly tournamentController: TournamentController,) {
    //
  }

  ionViewDidEnter() {
    this.id = this.navParams.get("id");
    window.history.pushState(null, null, '/' + this.id);
    this.tournamentController.setId(this.id);
    this.checkTournament();
    this.loadPlayerStats();
    this.loadLastTournaments();
  }

  handleError(error) {
    console.log('Failed to load data: ', error);

    if (this.navCtrl.getActive().name !== IndexPage.name) {
      this.navCtrl.push(IndexPage, error.error)
    }
  }

  refresh(refresher) {
    this.checkTournament(false)
      .then(() => refresher.complete());
  }

  checkTournament(loop = true) {
    return this.tournamentController.getRunningMatch()
      .then((match) => {
        if (match) {
          this.newMatch();
        } else {
          this.loadQueue();
          if (loop) {
            setTimeout(() => {
              this.checkTournament();
            }, 5000);
          }
        }
      })
      .catch(err => this.handleError(err));
  }

  loadQueue() {
    this.http.get<Array<Player>>("/api/tournament/" + this.id + "/queue")
      .subscribe(
        (queue: Array<Player>) => this.queue = queue,
        error => this.handleError(error)
      );
  }

  loadPlayerStats() {
    this.http.get<Array<PlayerSkill>>('/api/stats/' + this.id)
      .subscribe(
        (stats: Array<PlayerSkill>) => this.stats = stats,
        error => this.handleError(error)
      );
  }

  loadLastTournaments() {
    this.http.get<Array<Tournament>>('/api/tournament/' + this.id + '?num=10')
      .subscribe(
        (lastTournaments: Array<Tournament>) => this.lastTournaments = lastTournaments,
        error => this.handleError(error)
      );
  }

  newMatch() {
    this.navCtrl.push(MatchPage, {'id': this.id});
  }

  roundSkill(skill: number) {
    return skill ? skill.toFixed(2): '';
  }

  private wins(tournament: Tournament): { [key: string]: number } {
    console.log(tournament);
    return this.tournamentController.countWins(tournament.matches)
  }

  winsTeamA(tournament: Tournament): number {
    return this.wins(tournament)['teamA'];
  }

  winsTeamB(tournament: Tournament): number {
    return this.wins(tournament)['teamB'];
  }

  winRate(playerSkill: PlayerSkill): string {
    return ((playerSkill.wins / playerSkill.games) * 100).toFixed(2) + " %";
  }

}
