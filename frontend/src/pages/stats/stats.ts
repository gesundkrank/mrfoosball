import {Component} from "@angular/core";
import {NavController} from "ionic-angular";
import {Http} from "@angular/http";
import {MatchPage} from "../match/match";
import {Tournament, Player} from "../../providers/tournament";

const QUEUE_URL = '/api/tournament/queue';

@Component({
             selector: 'page-stats',
             templateUrl: 'stats.html'
           })
export class StatsPage {

  queue: Array<Player>;

  constructor(public http: Http,
              private readonly navCtrl: NavController,
              private readonly tournament: Tournament,) {
  }

  ionViewDidEnter() {
    this.checkTournament();
  }

  checkTournament() {
    this.tournament.getRunningMatch()
      .then((match) => {
        if (match) {
          this.newMatch();
        } else {
          this.loadQueue();
          let self = this;
          setTimeout(function() {
            self.checkTournament();
          }, 5000);
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
