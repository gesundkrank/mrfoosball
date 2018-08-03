import {Component} from "@angular/core";
import {NavController, NavParams} from "ionic-angular";
import {Http} from "@angular/http";
import {MatchPage} from "../match/match";
import {TournamentController} from "../../controllers/tournamentController";
import {Player, Tournament} from "../../models/tournament";
import {PlayerSkill} from "../../models/playerSkill";
import {IndexPage} from "../index";

@Component({
             selector: 'page-stats',
             templateUrl: 'stats.html'
           })
export class StatsPage {

  id: string;
  queue: Array<Player>;
  stats: Array<PlayerSkill>;
  lastTournaments: Array<Tournament>;

  constructor(public http: Http,
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
    console.log('Failed to load data: ', error.json());

    if (this.navCtrl.getActive().name !== IndexPage.name) {
      this.navCtrl.push(IndexPage, error.json())
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
    this.http.get("/api/tournament/" + this.id + "/queue")
      .map(res => res.json())
      .toPromise()
      .then(queue => {
        this.queue = queue;
      })
      .catch(err => this.handleError(err));
  }

  loadPlayerStats() {
    this.http.get('/api/stats/' + this.id)
      .map(res => res.json())
      .toPromise()
      .then(stats => {
        this.stats = stats;
      })
      .catch(err => this.handleError(err));
  }

  loadLastTournaments() {
    this.http.get('/api/tournament/' + this.id + '?num=10')
      .map(res => res.json())
      .toPromise()
      .then(lastTournaments => {
        this.lastTournaments = lastTournaments;
      })
      .catch(err => this.handleError(err));
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
