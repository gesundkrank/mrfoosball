import {Component} from "@angular/core";
import {NavController} from "ionic-angular";
import {Http} from "@angular/http";
import {MatchPage} from "../match/match";
import {TournamentController, Player} from "../../controllers/tournamentController";

const QUEUE_URL = '/api/tournament/queue';

@Component({
  selector: 'page-stats',
  templateUrl: 'stats.html'
})
export class StatsPage {

  queue: Array<Player>;

  constructor(
    public http: Http,
    private readonly navCtrl: NavController,
    private readonly tournament: TournamentController,
  ) {
    //
  }

  ionViewDidEnter() {
    this.checkTournament();
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
            let self = this;
            setTimeout(function() {
              self.checkTournament();
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

  newMatch() {
    this.navCtrl.push(MatchPage);
  }

}
