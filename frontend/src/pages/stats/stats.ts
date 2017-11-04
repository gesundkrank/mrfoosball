import {Component} from "@angular/core";
import {NavController} from "ionic-angular";
import {Http} from "@angular/http";
import {MatchPage} from "../match/match";
import {TournamentController} from "../../controllers/tournamentController";
import {Player} from "../../models/tournament";
import {PlayerSkill} from "../../models/playerSkill";

const QUEUE_URL = '/api/tournament/queue';
const STAT_URL = '/api/stats';

@Component({
  selector: 'page-stats',
  templateUrl: 'stats.html'
})
export class StatsPage {

  queue: Array<Player>;
  stats: Array<PlayerSkill>;

  constructor(
    public http: Http,
    private readonly navCtrl: NavController,
    private readonly tournament: TournamentController,
  ) {
    //
  }

  ionViewDidEnter() {
    this.checkTournament();
    this.loadStats();
  }

  refresh(refresher) {
    this.checkTournament(false)
      .then(() => refresher.complete());
  }

  checkTournament(loop = true) {
    return this.tournament.getRunningMatch()
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
  }

  loadQueue() {
    this.http.get(QUEUE_URL)
      .map(res => res.json())
      .toPromise()
      .then(queue => {
        this.queue = queue;
      });
  }

  loadStats() {
    this.http.get(STAT_URL)
      .map(res => res.json())
      .toPromise()
      .then(stats => {
        this.stats = stats;
      });
  }

  newMatch() {
    this.navCtrl.push(MatchPage);
  }

  roundSkill(skill) {
    return Math.round(skill);
  }

}
